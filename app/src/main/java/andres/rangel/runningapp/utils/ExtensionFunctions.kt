package andres.rangel.runningapp.utils

import andres.rangel.runningapp.R
import android.annotation.SuppressLint
import android.app.Activity
import android.widget.TextView
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

@SuppressLint("SetTextI18n")
fun Activity?.setNameInToolbar(name: String) {
    val tvTitle = this?.findViewById<TextView>(R.id.tvToolbarTitle)
    tvTitle?.text = "Let's go, $name!"
}