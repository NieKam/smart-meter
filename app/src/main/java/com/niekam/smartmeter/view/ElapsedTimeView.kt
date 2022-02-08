package com.niekam.smartmeter.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.niekam.smartmeter.R
import com.niekam.smartmeter.tools.now
import java.util.concurrent.TimeUnit


class ElapsedTimeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var timestamp: Long = 0L
        set(value) {
            field = value
            computePastTime(value)
        }

    private fun computePastTime(pastTimestamp: Long) {
        check(pastTimestamp < now()) { "timestamp cannot be greater than current time" }

        val diff = now() - pastTimestamp
        val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
        val hours = TimeUnit.MILLISECONDS.toHours(diff).toInt()
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()

        if (days == 0) {
            this.text = if (hours == 0) {
                // Display minutes
                if (minutes == 0) {
                    resources.getString(R.string.momentAgo)
                } else {
                    val minText = resources.getString(R.string.minutes, minutes)
                    resources.getString(R.string.lastMeasure, minText)
                }
            } else {
                // Display hours and minutes
                val hourText = resources.getQuantityString(R.plurals.hours, hours, hours)
                val realMinutes = minutes - TimeUnit.HOURS.toMinutes(hours.toLong()).toInt()
                val minText = resources.getString(R.string.minutes, realMinutes)

                resources.getString(R.string.lastMeasure, "$hourText $minText")
            }
            return
        }

        val dText = resources.getQuantityString(R.plurals.days, days, days)
        val realHours = hours - TimeUnit.DAYS.toHours(days.toLong()).toInt()
        val hText = resources.getQuantityString(R.plurals.hours, realHours, realHours)

        this.text = resources.getString(R.string.lastMeasure, "$dText $hText")
    }
}