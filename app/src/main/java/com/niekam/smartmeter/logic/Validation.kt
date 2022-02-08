package com.niekam.smartmeter.logic

import com.niekam.smartmeter.base.errors.NEW_VALUE_BIGGER
import com.niekam.smartmeter.base.errors.TIMESTAMP_TO_SHORT
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import java.util.concurrent.TimeUnit
import kotlin.math.abs


fun Measurement.validateNewMeasurement(measurements: List<Measurement>) {
    if (measurements.isEmpty()) {
        return
    }

    val last = measurements.last()
    compareMeasurements(this, last)
}

fun Measurement.validateUpdatedMeasurement(measurements: List<Measurement>) {
    if (measurements.size <= 1) {
        return
    }

    val thisElementId = measurements.indexOf(this)

    val previous = measurements[thisElementId - 1]
    compareMeasurements(this, previous)

    if (thisElementId == measurements.lastIndex) {
        return
    }

    val next = measurements[thisElementId + 1]
    compareMeasurements(next, this)
}

private fun compareMeasurements(newer: Measurement, older: Measurement) {
    if (newer.isBiggerThan(older)) {
        throw Throwable(NEW_VALUE_BIGGER)
    }

    if (newer.isTimestampTooShort(older)) {
        throw Throwable(TIMESTAMP_TO_SHORT)
    }
}

fun List<MeterWithMeasurements>.isValid(): Boolean {
    this.forEach { item ->
        if (item.meter.name.isEmpty()) {
            return false
        }

        item.measurements.let { list ->
            if (list.size > 1) {
                list.drop(1).forEachIndexed { index, measurement ->
                    val previousItem = list[index]
                    if (measurement.isBiggerThan(previousItem) || measurement.isTimestampTooShort(previousItem)) {
                        return false
                    }
                }
            }
        }
    }

    return true
}

private fun Measurement.isTimestampTooShort(last: Measurement): Boolean {
    return abs(this.timestamp - last.timestamp) < TimeUnit.HOURS.toMillis(1)
}

private fun Measurement.isBiggerThan(last: Measurement): Boolean {
    return if (this.timestamp < last.timestamp) {
        (this.value < last.value) && this.topUpValue == 0.0
    } else {
        (this.value > last.value) && this.topUpValue == 0.0
    }
}