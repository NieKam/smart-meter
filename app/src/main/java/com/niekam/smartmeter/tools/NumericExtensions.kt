package com.niekam.smartmeter.tools

import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.settings.CurrencyHolder
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

const val METER_ARG = "meter"

fun now() = Calendar.getInstance().timeInMillis

fun Double.convertToMoney(): String {
    return NumberFormat.getCurrencyInstance().apply {
        currency = CurrencyHolder.default
    }.format(this)
}

fun Long.timestampToDate(): String {
    return DateFormat.getDateInstance(DateFormat.SHORT).format(Date(this))
}

fun Long.timestampToDateAndTime(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(this))
}

fun Long.timestampToDayAndMonth(): String {
    val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dd/MM")
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(this))
}

fun Measurement.formatToCurrencyAndDate(): String {
    return "${this.value.convertToMoney()} (${this.timestamp.timestampToDate()})"
}