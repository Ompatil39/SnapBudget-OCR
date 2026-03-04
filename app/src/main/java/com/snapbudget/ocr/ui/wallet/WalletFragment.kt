package com.snapbudget.ocr.ui.wallet

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.snapbudget.ocr.R
import com.snapbudget.ocr.data.db.AppDatabase
import com.snapbudget.ocr.data.model.Category
import com.snapbudget.ocr.data.repository.TransactionRepository
import com.snapbudget.ocr.databinding.FragmentWalletBinding
import com.snapbudget.ocr.util.CurrencyFormatter

class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(database.transactionDao())
        val prefs = requireContext().getSharedPreferences("snapbudget_prefs", Context.MODE_PRIVATE)
        WalletViewModel.Factory(repository, prefs)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnSetBudget.setOnClickListener { showBudgetDialog() }
        binding.btnEditBudget.setOnClickListener { showBudgetDialog() }
        binding.cardBudgetOverview.setOnClickListener { showBudgetDialog() }
    }

    private fun setupObservers() {
        viewModel.hasBudget.observe(viewLifecycleOwner) { hasBudget ->
            binding.layoutWalletEmpty.visibility = if (hasBudget) View.GONE else View.VISIBLE
            binding.layoutWalletContent.visibility = if (hasBudget) View.VISIBLE else View.GONE
        }

        viewModel.monthlyBudget.observe(viewLifecycleOwner) { budget ->
            binding.tvBudgetAmount.text = CurrencyFormatter.format(budget)
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) { spent ->
            val budget = viewModel.monthlyBudget.value ?: 0.0
            val remaining = budget - spent
            val percent = if (budget > 0) (spent / budget * 100).coerceAtMost(100.0) else 0.0
            val percentInt = percent.toInt()

            binding.tvBudgetSpent.text = "${CurrencyFormatter.format(spent)} spent"
            binding.tvBudgetRemaining.text = "${CurrencyFormatter.format(kotlin.math.abs(remaining))} ${if (remaining >= 0) "remaining" else "over budget"}"
            binding.tvBudgetRemaining.setTextColor(Color.parseColor(if (remaining >= 0) "#00D68F" else "#FF6B6B"))
            binding.tvBudgetPercent.text = "$percentInt%"

            // Update progress bar
            val fillParams = binding.progressBudgetFill.layoutParams as LinearLayout.LayoutParams
            val remainParams = binding.progressBudgetRemain.layoutParams as LinearLayout.LayoutParams
            fillParams.weight = percentInt.toFloat()
            remainParams.weight = (100 - percentInt).toFloat()
            binding.progressBudgetFill.layoutParams = fillParams
            binding.progressBudgetRemain.layoutParams = remainParams
        }

        viewModel.categoryBudgets.observe(viewLifecycleOwner) { budgets ->
            buildCategoryBudgets(budgets)
        }
    }

    private fun buildCategoryBudgets(budgets: List<CategoryBudget>) {
        binding.layoutCategoryBudgets.removeAllViews()

        for (budget in budgets) {
            val catColor = try { Color.parseColor(budget.color) } catch (e: Exception) { Color.GRAY }
            val percentClamped = budget.percentage.coerceAtMost(100.0).toInt()

            // Determine progress bar color based on percentage
            val progressColor = when {
                budget.percentage >= 95 -> Color.parseColor("#FF6B6B")   // danger
                budget.percentage >= 80 -> Color.parseColor("#FF8C47")   // orange
                budget.percentage >= 60 -> Color.parseColor("#FFB547")   // warning
                else -> Color.parseColor("#00D68F")                       // positive
            }

            val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_3)
                }
                radius = resources.getDimensionPixelSize(R.dimen.radius_lg).toFloat()
                cardElevation = 0f
                setCardBackgroundColor(Color.parseColor("#161B24"))
                strokeColor = Color.parseColor("#12FFFFFF")
                strokeWidth = 1
            }

            val innerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val pad = resources.getDimensionPixelSize(R.dimen.spacing_4)
                setPadding(pad, pad, pad, pad)
            }

            // Row 1: Icon + Category Name + Over Budget label
            val row1 = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = android.widget.ImageView(requireContext()).apply {
                val cat = Category.fromName(budget.categoryName)
                setImageResource(cat.iconResId)
                setColorFilter(catColor)
                val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_2)
                }
            }
            row1.addView(icon)

            val catName = TextView(requireContext()).apply {
                text = budget.displayName
                setTextColor(Color.parseColor("#EEF0F4"))
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row1.addView(catName)

            if (budget.isOverBudget) {
                val overLabel = TextView(requireContext()).apply {
                    text = "OVER BUDGET"
                    setTextColor(Color.parseColor("#FF6B6B"))
                    textSize = 9f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                row1.addView(overLabel)
            }
            innerLayout.addView(row1)

            // Row 2: Spent / Budget
            val amounts = TextView(requireContext()).apply {
                text = "${CurrencyFormatter.format(budget.spentAmount)} / ${CurrencyFormatter.format(budget.budgetAmount)}"
                setTextColor(Color.parseColor("#8899AA"))
                textSize = 12f
                typeface = android.graphics.Typeface.MONOSPACE
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = resources.getDimensionPixelSize(R.dimen.spacing_1)
                layoutParams = lp
            }
            innerLayout.addView(amounts)

            // Row 3: Progress bar + percentage
            val progressRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val topMargin = resources.getDimensionPixelSize(R.dimen.spacing_2)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { this.topMargin = topMargin }
            }

            val progressTrack = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, resources.getDimensionPixelSize(R.dimen.spacing_1), 1f).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_2)
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 100f
                    setColor(Color.parseColor("#1FFFFFFF"))
                }
            }

            val fillView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, percentClamped.toFloat())
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 100f
                    setColor(progressColor)
                }
            }
            progressTrack.addView(fillView)

            val remainView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (100 - percentClamped).toFloat())
            }
            progressTrack.addView(remainView)

            progressRow.addView(progressTrack)

            val percentTv = TextView(requireContext()).apply {
                text = "${budget.percentage.toInt()}%"
                setTextColor(Color.parseColor("#8899AA"))
                textSize = 11f
            }
            progressRow.addView(percentTv)

            innerLayout.addView(progressRow)

            card.addView(innerLayout)
            binding.layoutCategoryBudgets.addView(card)
        }
    }

    private fun showBudgetDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Enter monthly budget (e.g. 20000)"
            val currentBudget = viewModel.monthlyBudget.value ?: 0.0
            if (currentBudget > 0) {
                setText(currentBudget.toInt().toString())
                selectAll()
            }
            val pad = resources.getDimensionPixelSize(R.dimen.spacing_5)
            setPadding(pad, pad, pad, pad)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Monthly Budget")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    viewModel.setMonthlyBudget(amount)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBudgetData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
