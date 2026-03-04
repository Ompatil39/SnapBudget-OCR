package com.snapbudget.ocr.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.snapbudget.ocr.databinding.FragmentWalletBinding

/**
 * Wallet fragment — placeholder for Phase 4.
 *
 * Will include:
 * - Monthly budget editor (top card)
 * - Per-category budget allocation with color-coded progress bars
 * - Budget status colors (green/yellow/orange/red)
 * - "OVER BUDGET" label for > 100%
 * - Empty states:
 *   a) No budget set (with CTA button)
 *   b) Budget set, 0 spending
 *
 * Currently shows the "No budget set" empty state with a CTA button.
 */
class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetBudget.setOnClickListener {
            // Phase 4: Open bottom sheet / dialog to input monthly budget
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
