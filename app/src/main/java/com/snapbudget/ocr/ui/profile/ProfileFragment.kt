package com.snapbudget.ocr.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.snapbudget.ocr.databinding.FragmentProfileBinding

/**
 * Profile fragment — Phase 5.
 *
 * Sections:
 * - User card (avatar + name + version)
 * - Preferences (Currency, Notifications)
 * - Data management (Export CSV, Clear All Data)
 * - About (Privacy Policy, Rate on Play Store, Open Source Licenses)
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                Toast.makeText(requireContext(), "Data cleared", Toast.LENGTH_SHORT).show()
                // TODO: Implement actual data clearing via ViewModel
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
