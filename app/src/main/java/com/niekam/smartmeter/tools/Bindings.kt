package com.niekam.smartmeter.tools

import android.annotation.SuppressLint
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.niekam.smartmeter.R
import com.niekam.smartmeter.data.model.INSUFFICIENT_MEASUREMENTS
import com.niekam.smartmeter.view.HealthBar
import java.util.concurrent.TimeUnit

@BindingAdapter("money_text")
fun bindMoneyToTextView(textView: AppCompatTextView, value: Double) {
    textView.text = value.convertToMoney()
}

@BindingAdapter("remaining_time")
fun bindRemainingMsToBarStatus(bar: HealthBar, remainingTimeMs: Long) {
    bar.daysLeft =
        if (remainingTimeMs < 0) {
            -1
        } else {
            TimeUnit.MILLISECONDS.toDays(remainingTimeMs - now()).toInt()
        }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("remaining_time")
fun bindRemainingMsToDays(textView: AppCompatTextView, remainingTimeMs: Long) {
    if (remainingTimeMs == INSUFFICIENT_MEASUREMENTS) {
        return
    }

    val days = TimeUnit.MILLISECONDS.toDays(remainingTimeMs - now()).toInt()
    val date = remainingTimeMs.timestampToDate()
    textView.text = "${textView.resources.getQuantityString(R.plurals.daysLeft, days, days)} ($date)"
}

@BindingAdapter("date_and_time")
fun bindTimestampDate(textView: AppCompatTextView, timestamp: Long) {
    textView.text = timestamp.timestampToDateAndTime()
}

@BindingAdapter("notification_icon")
fun bindNotificationStatusToIcon(imageView: AppCompatImageView, showNotification: Boolean) {
    val drawableId = if (showNotification) R.drawable.ic_notification_on else R.drawable.ic_notifications_off
    imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, drawableId))
}