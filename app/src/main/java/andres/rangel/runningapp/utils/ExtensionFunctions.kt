package andres.rangel.runningapp.utils

import java.text.SimpleDateFormat
import java.util.*

fun Long.dateFormat(): String {
    val time = this
    val calendar = Calendar.getInstance().apply {
        timeInMillis = time
    }
    val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    return dateFormat.format(calendar.time)
}