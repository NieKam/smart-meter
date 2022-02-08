package com.niekam.smartmeter.logic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.closeTo
import com.natpryce.hamkrest.equalTo
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.NO_BALANCE
import org.junit.Test
import java.util.concurrent.TimeUnit

class BalanceCalculatorKtTest {

    private val now = System.currentTimeMillis()

    @Test
    fun test_computeRemainingFunds_0() {
        val funds = listOf<Measurement>().computeRemainingFunds()
        assertThat(funds, closeTo(NO_BALANCE, 0.1))
    }

    @Test
    fun test_computeRemainingFunds_1() {
        val measurements = listOf(
            measurement(50.0, 4)
        )
        assertThat(measurements.computeRemainingFunds(), closeTo(50.0, 0.1))
    }

    @Test
    fun test_computeRemainingFunds_2() {
        val measurements = listOf(
            measurement(50.0, 5),
            measurement(47.0, 4)
        )

        assertThat(measurements.computeRemainingFunds(), closeTo(35.0, 0.1))
    }

    @Test
    fun test_computeRemainingTimeMs() {
        val measurements = listOf(
            measurement(50.0, 1),
            measurement(48.0, 0)
        )

        val expected = now + TimeUnit.DAYS.toMillis(24)

        assertThat(measurements.computeRemainingTimeMs(), equalTo(expected))
    }

    @Test
    fun test_computeAverageUsagePerDay_1() {
        val measurements = listOf(
            measurement(50.0, 4)
        )

        assertThat(measurements.computeAverageUsagePerDay(), closeTo(0.0, 0.1))
    }

    @Test
    fun test_computeAverageUsagePerDay_2() {
        val measurements = listOf(
            measurement(50.0, 1),
            measurement(46.0, 0)
        )

        assertThat(measurements.computeAverageUsagePerDay(), closeTo(4.0, 0.1))
    }

    @Test
    fun test_computeAverageUsagePerDay_3() {
        val measurements = listOf(
            measurement(50.0, 2),
            measurement(48.0, 1),
            measurement(44.0, 0)
        )

        assertThat(measurements.computeAverageUsagePerDay(), closeTo(3.82, 0.1))
    }

    @Test
    fun test_computeAverageUsagePerDay_4() {
        val measurements = listOf(
            measurement(50.0, 3),
            measurement(48.0, 2),
            measurement(46.0, 1),
            measurement(42.0, 0)
        )

        assertThat(measurements.computeAverageUsagePerDay(), closeTo(3.8, 0.1))
    }

    @Test
    fun test_computeAverageUsagePerDay_5() {
        val measurements = listOf(
            measurement(50.0, 4),
            measurement(48.0, 3),
            measurement(46.0, 2),
            measurement(42.0, 1),
            measurement(40.0, 0)
        )
        assertThat(measurements.computeAverageUsagePerDay(), closeTo(2.17, 0.1))
    }

    private fun measurement(value: Double, daysAgo: Long): Measurement {
        return Measurement(
            id = 0, topUpValue = 0.0, meterId = 0,
            timestamp = now - TimeUnit.DAYS.toMillis(daysAgo),
            value = value
        )
    }
}