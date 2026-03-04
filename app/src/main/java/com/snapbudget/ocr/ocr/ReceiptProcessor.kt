package com.snapbudget.ocr.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.text.Text
import com.snapbudget.ocr.data.model.Transaction
import com.snapbudget.ocr.data.db.TransactionDao
import com.snapbudget.ocr.categorization.CategoryClassifier
import com.snapbudget.ocr.matching.DuplicateDetector
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class ReceiptProcessor(
    private val context: Context,
    private val categoryClassifier: CategoryClassifier,
    private val transactionDao: TransactionDao? = null // Optional: for duplicate detection
) {
    private val ocrProcessor = OcrProcessor(context)
    private val receiptParser = ReceiptParser()
    private val receiptValidator = ReceiptValidator()
    private val aiFallbackService = GenerativeAiFallbackService(context)
    private val tag = "ReceiptProcessor"

    data class ProcessingResult(
        val transaction: Transaction,
        val rawOcrText: String,
        val confidenceScores: Map<String, Float>,
        val overallConfidence: Float,
        val validationIssues: List<String>,
        val imagePath: String?,
        // Duplicate detection results (§6)
        val isDuplicate: Boolean = false,
        val duplicateScore: Double = 0.0,
        val duplicateOfTransactionId: Long? = null,
        val duplicateConfidenceLabel: String = "N/A",
        // Receipt type classification (§9)
        val receiptType: String = "Unknown",
        // Anomaly detection (§10)
        val anomalyIssues: List<String> = emptyList(),
        // Confidence estimation (§12)
        val needsUserConfirmation: Boolean = false
    )

    suspend fun processReceipt(bitmap: Bitmap): ProcessingResult? {
        return try {
            Log.d(tag, "Processing receipt from bitmap")
            
            // Save bitmap to file
            val imagePath = saveBitmapToFile(bitmap)
            
            // Perform OCR
            val ocrResult = ocrProcessor.processImage(bitmap)
            
            when (ocrResult) {
                is OcrResult.Success -> {
                    processOcrResult(ocrResult.visionText, ocrResult.text, imagePath)
                }
                is OcrResult.Error -> {
                    Log.e(tag, "OCR Error: ${ocrResult.message}")
                    null
                }
                OcrResult.Cancelled -> {
                    Log.w(tag, "OCR Cancelled")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error processing receipt", e)
            null
        }
    }

    suspend fun processReceipt(uri: Uri): ProcessingResult? {
        return try {
            Log.d(tag, "Processing receipt from URI")
            
            // Perform OCR
            val ocrResult = ocrProcessor.processImage(uri)
            
            when (ocrResult) {
                is OcrResult.Success -> {
                    processOcrResult(ocrResult.visionText, ocrResult.text, uri.toString())
                }
                is OcrResult.Error -> {
                    Log.e(tag, "OCR Error: ${ocrResult.message}")
                    null
                }
                OcrResult.Cancelled -> {
                    Log.w(tag, "OCR Cancelled")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error processing receipt", e)
            null
        }
    }

    private suspend fun processOcrResult(visionText: Text, rawText: String, imagePath: String?): ProcessingResult? {
        Log.d(tag, "processOcrResult | Step 1: Starting OCR result processing")

        // Parse receipt deterministically first
        val parsedReceipt = receiptParser.parse(visionText, rawText)
            ?: run {
                Log.w(tag, "processOcrResult | Step 2: Parsing returned null, aborting")
                return null
            }
        Log.d(tag, "processOcrResult | Step 2: Parsing complete - merchant='${parsedReceipt.merchantName}', amount=${parsedReceipt.totalAmount}")

        // Validate extracted data
        val validationResult = receiptValidator.validate(parsedReceipt)
        Log.d(tag, "processOcrResult | Step 3: Validation - valid=${validationResult.isValid}, issues=${validationResult.issues}")

        // Fallback to Gemini AI if the deterministic confidence is too low (<60%) or critical fields missing
        var finalMerchantName = parsedReceipt.merchantName
        var finalTotalAmount = parsedReceipt.totalAmount
        var finalDate = parsedReceipt.date
        var finalGstNumber = parsedReceipt.gstNumber
        var finalConfidence = parsedReceipt.overallConfidence
        val finalConfidenceScores = parsedReceipt.confidenceScores.toMutableMap()
        
        if (finalConfidence < 0.6f || finalTotalAmount <= 0.0 || finalMerchantName == "Unknown Merchant") {
            Log.d(tag, "processOcrResult | Step 4: Low confidence (${finalConfidence}). Engaging Gemini AI fallback...")
            val fallbackResult = aiFallbackService.analyzeReceipt(rawText, finalConfidence)
            if (fallbackResult != null) {
                if (fallbackResult.merchantName != null) {
                    finalMerchantName = fallbackResult.merchantName
                    finalConfidenceScores["merchant"] = 0.9f
                }
                if (fallbackResult.totalAmount != null) {
                    finalTotalAmount = fallbackResult.totalAmount
                    finalConfidenceScores["amount"] = 0.9f
                }
                if (fallbackResult.gstNumber != null) {
                    finalGstNumber = fallbackResult.gstNumber
                    finalConfidenceScores["gst"] = 0.9f
                }
                finalConfidence = Math.max(finalConfidence.toDouble(), fallbackResult.confidenceScore.toDouble()).toFloat()
                finalConfidenceScores["ai_fallback_triggered"] = 1.0f
                Log.d(tag, "processOcrResult | Step 5c: Gemini fallback success. merchant='$finalMerchantName', amount=₹$finalTotalAmount")
            } else {
                Log.w(tag, "processOcrResult | Step 4: Fallback failed or skipped.")
            }
        } else {
            Log.d(tag, "processOcrResult | Step 4: Confidence sufficient ($finalConfidence), skipping Gemini fallback")
        }
        
        // Classify category - Use advanced text scanning as primary, but respect user overrides
        val userCategory = categoryClassifier.getUserRule(finalMerchantName)
        val category = if (userCategory != null) {
            Log.d(tag, "processOcrResult | Step 6: Category from user override: ${userCategory.name}")
            userCategory
        } else {
            val evaluatedCategory = parsedReceipt.categoryAnalysis.finalCategory
            Log.d(tag, "processOcrResult | Step 6: Category from deep scanning: ${evaluatedCategory.name}")
            evaluatedCategory
        }

        // ====================================================================
        // Duplicate Detection (Guide §6)
        // ====================================================================
        var isDuplicate = false
        var duplicateScore = 0.0
        var duplicateOfId: Long? = null
        var duplicateLabel = "N/A"

        if (transactionDao != null) {
            try {
                Log.d(tag, "processOcrResult | Step 7: Running duplicate detection...")
                // Get recent transactions (last 50) for comparison
                val recentTransactions = transactionDao.getRecentTransactionsSync(50)
                val dupResult = DuplicateDetector.checkForDuplicate(
                    newMerchantName = finalMerchantName,
                    newAmount = finalTotalAmount,
                    newDate = finalDate,
                    existingTransactions = recentTransactions
                )
                if (dupResult != null) {
                    isDuplicate = dupResult.isDuplicate
                    duplicateScore = dupResult.score
                    duplicateOfId = dupResult.existingTransactionId
                    duplicateLabel = dupResult.confidenceLabel
                    Log.d(tag, "processOcrResult | Step 7: Duplicate check result:")
                    Log.d(tag, "     - isDuplicate=$isDuplicate, score=%.4f, threshold=%.2f".format(dupResult.score, dupResult.threshold))
                    Log.d(tag, "     - merchantScore=%.4f, amountScore=%.4f, dateScore=%.4f".format(dupResult.merchantScore, dupResult.amountScore, dupResult.dateScore))
                    Log.d(tag, "     - existingTxId=${dupResult.existingTransactionId}, label=$duplicateLabel")

                    if (isDuplicate) {
                        finalConfidenceScores["duplicate_detected"] = dupResult.score.toFloat()
                    }
                } else {
                    Log.d(tag, "processOcrResult | Step 7: No potential duplicates found")
                }
            } catch (e: Exception) {
                Log.e(tag, "processOcrResult | Step 7: Duplicate detection failed", e)
            }
        } else {
            Log.d(tag, "processOcrResult | Step 7: TransactionDao not available, skipping duplicate detection")
        }

        // Create transaction
        val transaction = Transaction(
            merchantName = finalMerchantName,
            amount = finalTotalAmount,
            date = finalDate,
            category = category.name,
            gstNumber = finalGstNumber,
            rawOcrText = rawText,
            confidenceScore = parsedReceipt.confidenceReport?.overallScore ?: finalConfidence,
            imagePath = imagePath
        )

        // Use the confidence report from the parser if available
        val resolvedConfidence = parsedReceipt.confidenceReport?.overallScore ?: finalConfidence
        val mergedConfidenceScores = finalConfidenceScores.toMutableMap()
        for ((key, value) in parsedReceipt.confidenceScores) {
            mergedConfidenceScores.putIfAbsent(key, value)
        }

        // Merge validation + anomaly issues
        val allIssues = validationResult.issues + parsedReceipt.anomalyIssues
        val needsConfirmation = parsedReceipt.confidenceReport?.needsUserConfirmation ?: (resolvedConfidence < 0.65f)

        Log.d(tag, "processOcrResult | Step 8: Transaction created - merchant='${transaction.merchantName}', amount=₹${transaction.amount}, category=${transaction.category}")
        Log.d(tag, "processOcrResult | Step 9: receiptType=${parsedReceipt.receiptType.displayName}, anomalies=${parsedReceipt.anomalyIssues.size}, needsConfirmation=$needsConfirmation")

        return ProcessingResult(
            transaction = transaction,
            rawOcrText = rawText,
            confidenceScores = mergedConfidenceScores,
            overallConfidence = resolvedConfidence,
            validationIssues = allIssues,
            imagePath = imagePath,
            isDuplicate = isDuplicate,
            duplicateScore = duplicateScore,
            duplicateOfTransactionId = duplicateOfId,
            duplicateConfidenceLabel = duplicateLabel,
            receiptType = parsedReceipt.receiptType.displayName,
            anomalyIssues = parsedReceipt.anomalyIssues,
            needsUserConfirmation = needsConfirmation
        )
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val filename = "receipt_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, "receipts")
        if (!file.exists()) file.mkdirs()
        
        val imageFile = File(file, filename)
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        return imageFile.absolutePath
    }

    fun close() {
        ocrProcessor.close()
    }

    data class ValidationResult(
        val isValid: Boolean,
        val issues: List<String>
    )

    inner class ReceiptValidator {
        fun validate(receipt: ReceiptParser.ParsedReceipt): ValidationResult {
            val issues = mutableListOf<String>()
            
            // Check merchant name
            if (receipt.merchantName.isEmpty() || 
                receipt.merchantName == "Unknown" || 
                receipt.merchantName == "Unknown Merchant") {
                issues.add("Merchant name could not be extracted clearly")
            }
            
            // Check amount
            if (receipt.totalAmount <= 0) {
                issues.add("Invalid amount detected")
            } else if (receipt.totalAmount > 100000) {
                issues.add("Unusually high amount - please verify")
            }
            
            // Check date — flag if receipt is more than 1 year old
            val oneYearAgo = java.util.Calendar.getInstance()
            oneYearAgo.add(java.util.Calendar.YEAR, -1)
            if (receipt.date.before(oneYearAgo.time)) {
                issues.add("Receipt date is very old - please verify")
            }
            
            // Check GST if present
            if (receipt.gstNumber != null && !isValidGST(receipt.gstNumber)) {
                issues.add("GST number format appears invalid")
            }
            
            // Cross-verify line items with total
            val lineItemsSum = receipt.lineItems.sumOf { it.totalPrice }
            if (lineItemsSum > 0 && receipt.totalAmount > 0) {
                val difference = kotlin.math.abs(lineItemsSum - receipt.totalAmount)
                val percentageDiff = (difference / receipt.totalAmount) * 100
                
                if (percentageDiff > 20) {
                    issues.add("Line items sum (₹%.2f) doesn't match total (₹%.2f)".format(lineItemsSum, receipt.totalAmount))
                }
            }
            
            // Check overall confidence
            if (receipt.overallConfidence < 0.5) {
                issues.add("Low confidence in extraction - please review all fields")
            }
            
            return ValidationResult(
                isValid = issues.isEmpty(),
                issues = issues
            )
        }
        
        private fun isValidGST(gst: String): Boolean {
            if (gst.length != 15) return false
            
            // Check state code
            val stateCode = gst.substring(0, 2).toIntOrNull() ?: return false
            if (stateCode !in 1..37) return false
            
            // Check PAN format (simplified)
            val panPart = gst.substring(2, 12)
            return panPart.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))
        }
    }
}