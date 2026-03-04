package com.snapbudget.ocr.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.snapbudget.ocr.databinding.FragmentAnalyticsBinding

/**
 * Analytics fragment — placeholder for Phase 3.
 *
 * Will include:
 * - LineChart for spending trends (7D/30D/90D)
 * - PieChart / DonutChart for category breakdown
 * - Category breakdown list with horizontal progress bars
 * - Top Merchants section ranked by total spend
 * - Date-range picker
 *
 * Currently shows the empty state defined in fragment_analytics.xml.
 */
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Phase 3 implementation will go here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
