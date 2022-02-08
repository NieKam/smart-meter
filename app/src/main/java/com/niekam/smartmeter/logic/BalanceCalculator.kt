package com.niekam.smartmeter.logic

import com.niekam.smartmeter.data.model.INSUFFICIENT_MEASUREMENTS
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.NO_BALANCE
import com.niekam.smartmeter.tools.now
import java.util.concurrent.TimeUnit

private const val W1 = 1.0
private const val W2 = 0.1
private const val W3 = 0.05
private const val W4 = 0.0025

fun List<Measurement>.computeRemainingFunds(): Double {
    return when (this.size) {
        0 -> return NO_BALANCE
        1 -> return this.first().value
        else -> {
            approximateFunds(this)
        }
    }
}

private fun approximateFunds(measurements: List<Measurement>): Double {
    val last = measurements.last()
    val diffHours = TimeUnit.MILLISECONDS.toHours(now() - last.timestamp)
    val usagePerHour = approximateAverageUsagePerHour(measurements)

    return last.value - (diffHours * usagePerHour)
}

fun List<Measurement>.computeRemainingTimeMs(): Long {
    if (this.size < 2) {
        return INSUFFICIENT_MEASUREMENTS
    }
    val last = this.last()
    val usagePerHour = approximateAverageUsagePerHour(this)
    val hoursLeft = (last.value / usagePerHour).toLong()

    return last.timestamp + TimeUnit.HOURS.toMillis(hoursLeft)
}

fun List<Measurement>.computeAverageUsagePerDay(): Double {
    if (this.size < 2) {
        return 0.0
    }

    return this.approximateUsage()
}

private fun List<Measurement>.approximateUsage(): Double {
    return approximateAverageUsagePerHour(this) * TimeUnit.DAYS.toHours(1)
}

private fun approximateAverageUsagePerHour(measurements: List<Measurement>): Double {
    check(measurements.size > 1) { "Size cannot be less than 2" }

    return when (measurements.size) {
        2 -> averageUsageFor2Measurements(measurements.takeLast(2))
        3 -> averageUsageFor3Measurements(measurements.takeLast(3))
        4 -> averageUsageFor4Measurements(measurements.takeLast(4))
        5 -> averageUsageFor5Measurements(measurements.takeLast(5))
        else -> averageUsageFor5Measurements(measurements.takeLast(5))
    }
}

private fun approximateUsage(last: Measurement, secondToLast: Measurement): Double {
    val diffHours = TimeUnit.MILLISECONDS.toHours(last.timestamp - secondToLast.timestamp)
    val fundsDiff = ((secondToLast.value - last.value) + last.topUpValue)
    return fundsDiff / diffHours
}

private fun averageUsageFor2Measurements(lastElements: List<Measurement>): Double {
    check(lastElements.size >= 2)

    val last = lastElements.last()
    val secondToLast = lastElements[lastElements.lastIndex - 1]
    return approximateUsage(last, secondToLast)
}

private fun averageUsageFor3Measurements(lastElements: List<Measurement>): Double {
    check(lastElements.size >= 3)

    val lastIndex = lastElements.lastIndex

    val last = lastElements.last()
    val secondToLast = lastElements[lastIndex - 1]
    val thirdToLAst = lastElements[lastIndex - 2]

    val lastApproximateUsage = approximateUsage(last, secondToLast)
    val secondApproximateUsage = approximateUsage(secondToLast, thirdToLAst)

    return ((secondApproximateUsage * W2) + (lastApproximateUsage * W1)) / (W1 + W2)
}

private fun averageUsageFor4Measurements(lastElements: List<Measurement>): Double {
    check(lastElements.size >= 4)
    val lastIndex = lastElements.lastIndex

    val last = lastElements.last()
    val secondToLast = lastElements[lastIndex - 1]
    val thirdToLAst = lastElements[lastIndex - 2]
    val fourthToLast = lastElements[lastIndex - 3]

    val lastApproximateUsage = approximateUsage(last, secondToLast)
    val secondApproximateUsage = approximateUsage(secondToLast, thirdToLAst)
    val thirdApproximateUsage = approximateUsage(thirdToLAst, fourthToLast)

    return ((thirdApproximateUsage * W3) + (secondApproximateUsage * W2) + (lastApproximateUsage * W1)) / (W1 + W2 + W3)
}

private fun averageUsageFor5Measurements(lastElements: List<Measurement>): Double {
    check(lastElements.size >= 5)
    val lastIndex = lastElements.lastIndex

    val last = lastElements.last()
    val secondToLast = lastElements[lastIndex - 1]
    val thirdToLAst = lastElements[lastIndex - 2]
    val fourthToLast = lastElements[lastIndex - 3]
    val fifthToLast = lastElements[lastIndex - 4]

    val lastApproximateUsage = approximateUsage(last, secondToLast)
    val secondApproximateUsage = approximateUsage(secondToLast, thirdToLAst)
    val thirdApproximateUsage = approximateUsage(thirdToLAst, fourthToLast)
    val fourthApproximateUsage = approximateUsage(fourthToLast, fifthToLast)

    return ((fourthApproximateUsage * W4) + (thirdApproximateUsage * W3) + (secondApproximateUsage * W2) + (lastApproximateUsage * W1)) / (W1 + W2 + W3 + W4)
}


