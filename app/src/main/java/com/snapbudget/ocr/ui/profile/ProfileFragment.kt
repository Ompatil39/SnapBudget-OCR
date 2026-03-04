package com.snapbudget.ocr.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.snapbudget.ocr.data.db.AppDatabase
import com.snapbudget.ocr.data.repository.TransactionRepository
import com.snapbudget.ocr.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.rowExport.setOnClickListener {
            Toast.makeText(requireContext(), "Export coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.rowClearData.setOnClickListener {
            showClearDataConfirmation()
        }
    }

    private fun showClearDataConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all transactions and budgets. This cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                lifecycleScope.launch {
                    val database = AppDatabase.getDatabase(requireContext())
                    val repository = TransactionRepository(database.transactionDao())
                    repository.deleteAllTransactions()

                    // Clear budget prefs
                    requireContext()
                        .getSharedPreferences("snapbudget_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()

                    Toast.makeText(requireContext(), "All data cleared", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
