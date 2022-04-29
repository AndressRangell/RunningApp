package andres.rangel.runningapp.ui.fragment

import andres.rangel.runningapp.R
import andres.rangel.runningapp.databinding.FragmentStatisticsBinding
import andres.rangel.runningapp.ui.viewmodel.StatisticsViewModel
import andres.rangel.runningapp.utils.TrackingUtility
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.round
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
    }

}