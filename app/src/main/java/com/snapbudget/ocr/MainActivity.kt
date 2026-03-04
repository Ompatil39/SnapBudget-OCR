package com.snapbudget.ocr

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.snapbudget.ocr.data.db.AppDatabase
import com.snapbudget.ocr.data.model.Category
import com.snapbudget.ocr.data.model.Transaction
import com.snapbudget.ocr.data.repository.TransactionRepository
import com.snapbudget.ocr.databinding.ActivityMainBinding
import com.snapbudget.ocr.ui.camera.CameraActivity
import com.snapbudget.ocr.ui.dashboard.DashboardViewModel
import com.snapbudget.ocr.ui.dashboard.TransactionAdapter
import com.snapbudget.ocr.ui.history.TransactionHistoryActivity
import com.snapbudget.ocr.ui.receipt.ReceiptPreviewActivity
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar is hidden in this design — custom header used
        setSupportActionBar(binding.toolbar)
        supportActionBar?.hide()

        // Setup ViewModel
        val database = AppDatabase.getDatabase(this)
        val repository = TransactionRepository(database.transactionDao())
        viewModel = ViewModelProvider(this, DashboardViewModel.Factory(repository))
            .get(DashboardViewModel::class.java)

        setupGreeting()
        setupRecyclerView()
        setupPieChart()
        setupObservers()
        setupClickListeners()
        
        // Set initial month label
        val calendar = Calendar.getInstance()
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        binding.tvCurrentMonth.text = "${monthNames[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.YEAR)}"
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemClick = { transaction ->
                openTransactionDetail(transaction)
            },
            onDeleteClick = { transaction ->
                showDeleteConfirmation(transaction)
            }
        )

        binding.recyclerRecentTransactions.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentTransactions.adapter = adapter
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setHoleColor(android.graphics.Color.parseColor("#1E2535"))
            setCenterTextColor(android.graphics.Color.parseColor("#EEF0F4"))
            setExtraBottomOffset(16f)
            holeRadius = 45f
            transparentCircleRadius = 50f
            setTransparentCircleColor(android.graphics.Color.parseColor("#1E2535"))
            setTransparentCircleAlpha(120)
            
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                isWordWrapEnabled = true
                isEnabled = true
                textSize = 11f
                formSize = 10f
                xEntrySpace = 14f
                yEntrySpace = 4f
                textColor = android.graphics.Color.parseColor("#8E97AA")
            }
        }
    }

    private fun setupObservers() {
        viewModel.monthlyTotal.observe(this) { total ->
            binding.tvMonthlyTotal.text = String.format(Locale.getDefault(), "₹%.2f", total ?: 0.0)
        }

        viewModel.pieChartEntries.observe(this) { entries ->
            updatePieChart(entries)
        }

        viewModel.recentTransactions.observe(this) { transactions ->
            adapter.submitList(transactions)
            
            if (transactions.isNullOrEmpty()) {
                binding.recyclerRecentTransactions.visibility = View.GONE
                binding.tvNoTransactions.visibility = View.VISIBLE
            } else {
                binding.recyclerRecentTransactions.visibility = View.VISIBLE
                binding.tvNoTransactions.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updatePieChart(entries: List<PieEntry>) {
        if (entries.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            return
        }

        binding.pieChart.visibility = View.VISIBLE

        val colors = entries.mapNotNull { entry ->
            try {
                val category = Category.fromName(entry.label)
                android.graphics.Color.parseColor(category.color)
            } catch (e: Exception) {
                android.graphics.Color.GRAY
            }
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            this.colors = colors
            valueTextSize = 11f
            valueTextColor = android.graphics.Color.parseColor("#EEF0F4")
            valueFormatter = PercentFormatter(binding.pieChart)
            sliceSpace = 3f
        }

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    private fun setupClickListeners() {
        binding.btnScanReceipt.setOnClickListener {
            openCamera()
        }

        // FAB also opens camera
        binding.fabAddManual.setOnClickListener {
            openCamera()
        }

        binding.tvViewAll.setOnClickListener {
            openTransactionHistory()
        }

        binding.btnViewHistory.setOnClickListener {
            openTransactionHistory()
        }

        binding.cardMonthlyTotal.setOnClickListener {
            showMonthSelector()
        }
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun openManualEntry() {
        val intent = Intent(this, ReceiptPreviewActivity::class.java)
        startActivity(intent)
    }

    private fun openTransactionHistory() {
        val intent = Intent(this, TransactionHistoryActivity::class.java)
        startActivity(intent)
    }

    private fun openTransactionDetail(transaction: Transaction) {
        val intent = Intent(this, ReceiptPreviewActivity::class.java).apply {
            putExtra(ReceiptPreviewActivity.EXTRA_TRANSACTION_ID, transaction.id)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmation(transaction: Transaction) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMonthSelector() {
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Month")
            .setItems(months) { _, which ->
                viewModel.loadDataForMonth(currentYear, which)
                binding.tvCurrentMonth.text = "${months[which]} $currentYear"
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                openTransactionHistory()
                true
            }
            R.id.action_settings -> {
                // Open settings
                true
            }
            R.id.action_search -> {
                openTransactionHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDashboardData()
    }
}