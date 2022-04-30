package andres.rangel.runningapp.utils

import andres.rangel.runningapp.R
import andres.rangel.runningapp.db.Run
import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

@SuppressLint("ViewConstructor")
class CustomMarkerView(
    val runs: List<Run>,
    context: Context,
    layoutId: Int
) : MarkerView(context, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null)
            return

        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvAvgSpeed = findViewById<TextView>(R.id.tvAvgSpeed)
        val tvDistance = findViewById<TextView>(R.id.tvDistance)
        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        val tvCaloriesBurned = findViewById<TextView>(R.id.tvCaloriesBurned)

        tvDate.text = run.timestamp.dateFormat()
        tvAvgSpeed.text = "${run.averageSpeedInKmH} km/h"
        tvDistance.text = "${run.distanceInMeters / 1000f} km"
        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
        tvCaloriesBurned.text = "${run.caloriesBurned} kcal"
    }

}