package andres.rangel.runningapp.ui.viewmodel

import andres.rangel.runningapp.repositories.MainRepository
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    mainRepository: MainRepository
) : ViewModel() {

    val totalAverageSpeed = mainRepository.getTotalAverageSpeed()
    val totalDistance = mainRepository.getTotalDistance()
    val totalTime = mainRepository.getTotalTime()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

}