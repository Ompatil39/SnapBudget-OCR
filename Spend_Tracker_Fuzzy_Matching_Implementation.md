# Spend Tracker -- Fuzzy Matching & Normalization Implementation Guide

## 1. Objective

Design and implement a production-ready fuzzy matching and normalization
system for a receipt-based spend tracker application. The system must:

-   Normalize merchant names from noisy OCR output
-   Categorize line items accurately
-   Detect duplicate transactions
-   Provide confidence scoring
-   Support Indian retail naming variations
-   Maintain high accuracy with lightweight processing suitable for
    Android deployment

------------------------------------------------------------------------

## 2. System Architecture

    OCR Output
        ↓
    Text Cleaning & Normalization
        ↓
    ├── Merchant Matching Engine
    ├── Line Item Category Engine
    └── Duplicate Detection Engine
        ↓
    Confidence Scoring Layer
        ↓
    User Confirmation (if required)

Each module must return a confidence score to enable decision-level
validation.

------------------------------------------------------------------------

## 3. Text Cleaning & Normalization Layer

### 3.1 Standardization Steps

-   Convert text to lowercase
-   Remove punctuation
-   Remove long numeric strings (GST, invoice numbers)
-   Replace special symbols (& → and)
-   Remove extra whitespace

### 3.2 Indian Retail Stopword Removal

Remove common non-distinct tokens:

-   pvt
-   ltd
-   private
-   limited
-   supermarket
-   mart
-   enterprises
-   traders
-   store

This significantly improves matching precision.

------------------------------------------------------------------------

## 4. Merchant Matching Engine

### 4.1 Strategy

Use a hybrid approach:

1.  Token-based normalization
2.  Jaro-Winkler similarity for final scoring

### 4.2 Why Jaro-Winkler

-   Strong performance for short strings
-   Rewards prefix similarity
-   Suitable for business names

### 4.3 Similarity Threshold

-   Merchant match threshold: 0.88 -- 0.92

### 4.4 Implementation Logic

1.  Normalize merchant string
2.  Remove stopwords
3.  Sort tokens
4.  Compare against stored canonical merchants
5.  Select highest similarity above threshold

------------------------------------------------------------------------

## 5. Line Item Category Mapping Engine

### 5.1 Hybrid Keyword + Fuzzy Model

Step 1: Maintain structured keyword dictionary.

Example:

-   milk → Dairy
-   amul → Dairy
-   paneer → Dairy
-   petrol → Fuel
-   diesel → Fuel

Step 2: If no exact keyword match:

-   Apply fuzzy matching against dictionary keys
-   Accept if similarity ≥ 0.80 -- 0.85

### 5.2 Design Considerations

-   Token-level comparison preferred over full-string comparison
-   Store user corrections for adaptive learning
-   Assign category confidence score

------------------------------------------------------------------------

## 6. Duplicate Detection Engine

### 6.1 Weighted Scoring Model

    Duplicate Score =
    0.5 × Merchant Similarity
    + 0.3 × Amount Match
    + 0.2 × Date Match

### 6.2 Duplicate Threshold

-   Mark as duplicate if score ≥ 0.90

### 6.3 Matching Rules

-   Merchant → fuzzy match
-   Amount → exact match
-   Date → exact or ±1 day tolerance

------------------------------------------------------------------------

## 7. Confidence Scoring Framework

Each module must return:

-   Result
-   Similarity score
-   Threshold used
-   Confidence label (High / Medium / Low)

If confidence \< threshold:

-   Prompt user confirmation
-   Store corrected mapping locally

------------------------------------------------------------------------

## 8. Performance & Deployment Guidelines

-   Use lightweight string similarity libraries
-   Avoid heavy ML models for on-device processing
-   Cache canonical merchants
-   Optimize matching for short strings (\< 50 characters)
-   Perform benchmarking with real receipts

------------------------------------------------------------------------

## 9. Suggested Similarity Thresholds

  Use Case              Threshold Range
  --------------------- -----------------
  Merchant Matching     0.88 -- 0.92
  Category Mapping      0.80 -- 0.85
  Duplicate Detection   0.90+

Thresholds must be validated using real-world receipt datasets.

------------------------------------------------------------------------

## 10. Production Recommendations

### Implement

-   Stopword removal layer
-   Token-based normalization
-   Jaro-Winkler similarity
-   Keyword + fuzzy hybrid category mapping
-   Weighted duplicate detection scoring
-   User correction learning system

### Avoid (for MVP / Hackathon)

-   Heavy NLP pipelines
-   Deep learning embeddings
-   Cloud-dependent matching systems
-   Full semantic vector models

------------------------------------------------------------------------

## 11. Accuracy Consideration

High OCR accuracy does not guarantee financial extraction accuracy.

Accuracy must be measured at:

-   Merchant identification level
-   Category mapping level
-   Total extraction level
-   Duplicate detection level

Implement layered validation and confidence scoring to achieve
near-production reliability.

------------------------------------------------------------------------

## 12. Future Enhancements (Optional)

-   Embedding-based similarity (Sentence-BERT)
-   Merchant clustering using vector similarity
-   Backend-based ML scoring engine
-   Analytics-driven threshold tuning

------------------------------------------------------------------------

## Document Version

Version: 1.0\
Status: Production Implementation Blueprint\
Target Platform: Android (On-device processing)
