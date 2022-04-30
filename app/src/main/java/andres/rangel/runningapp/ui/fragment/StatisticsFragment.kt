package andres.rangel.runningapp.ui.fragment

import andres.rangel.runningapp.R
import andres.rangel.runningapp.databinding.FragmentStatisticsBinding
import andres.rangel.runningapp.ui.viewmodel.StatisticsViewModel
import andres.rangel.runningapp.utils.CustomMarkerView
import andres.rangel.runningapp.utils.TrackingUtility
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var binding: FragmentStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    private fun setupBarChart() {
        binding.apply {
            barChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawLabels(false)
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            barChart.axisLeft.apply {
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            barChart.axisRight.apply {
                axisLineColor = Color.WHITE
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            barChart.apply {
                description.text = "Average Speed Over Time"
                legend.isEnabled = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToObservers() {
        viewModel.totalAverageSpeed.observe(viewLifecycleOwner) {
            it?.let {
                val averageSpeed = (it * 10f).roundToInt() / 10f
                binding.tvAverageSpeed.text = "$averageSpeed km/h"
            }
        }
        viewModel.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                val totalDistance = ((it / 1000f) * 10f).roundToInt() / 10f
                binding.tvTotalDistance.text = "$totalDistance km"
            }
        }
        viewModel.totalTime.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalTime.text = TrackingUtility.getFormattedStopWatchTime(it)
            }
        }
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalCalories.text = "$it kcal"
            }
        }
        viewModel.runsSortedByDate.observe(viewLifecycleOwner) {
            it?.let {
                val allAverageSpeeds = it.indices.map { i ->
                    BarEntry(i.toFloat(), it[i].averageSpeedInKmH)
                }
                val barDataSet = BarDataSet(allAverageSpeeds, "Average Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                binding.apply {
                    barChart.data = BarData(barDataSet)
                    barChart.marker =
                        CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
                    barChart.invalidate()
                }
            }
        }
    }

}