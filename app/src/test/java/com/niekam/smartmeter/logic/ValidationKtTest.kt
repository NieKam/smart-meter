package com.niekam.smartmeter.logic

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.niekam.smartmeter.base.errors.NEW_VALUE_BIGGER
import com.niekam.smartmeter.base.errors.TIMESTAMP_TO_SHORT
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import junit.framework.Assert.assertFalse
import junit.framework.Assert.fail
import org.junit.Test
import java.util.concurrent.TimeUnit

class ValidationKtTest {

    @Test
    fun `test when new value is bigger`() {
        val measurements = generate(5)
        val newMeasurement =
            Measurement(id = 6, timestamp = System.currentTimeMillis(), value = 10.0, topUpValue = 0.0, meterId = 1)

        try {
            newMeasurement.validateNewMeasurement(measurements)
            fail()
        } catch (e: Throwable) {
            assertThat(e.message, equalTo(NEW_VALUE_BIGGER))
        }
    }

    @Test
    fun `validate if new measurement is added too fast`() {
        val now = System.currentTimeMillis()

        val measurements = listOf(
            Measurement(
                id = 6,
                timestamp = now - TimeUnit.MINUTES.toMillis(5),
                value = 10.0,
                topUpValue = 0.0,
                meterId = 1
            )
        )
        val newMeasurement =
            Measurement(id = 6, timestamp = now, value = 8.0, topUpValue = 0.0, meterId = 1)

        try {
            newMeasurement.validateNewMeasurement(measurements)
            fail()
        } catch (e: Throwable) {
            assertThat(e.message, equalTo(TIMESTAMP_TO_SHORT))
        }
    }

    @Test
    fun `test when new measurement is correct`() {
        val measurements = generate(5)
        val newMeasurement =
            Measurement(id = 6, timestamp = System.currentTimeMillis(), value = 0.0, topUpValue = 0.0, meterId = 1)

        newMeasurement.validateNewMeasurement(measurements)
    }

    @Test
    fun `test when updated timestamp is too fast - previous element`() {
        val measurements = generate(5)
        val updatedMeasurement = measurements[3]
        updatedMeasurement.timestamp = measurements[2].timestamp

        try {
            updatedMeasurement.validateUpdatedMeasurement(measurements)
            fail()
        } catch (e: Throwable) {
            assertThat(e.message, equalTo(TIMESTAMP_TO_SHORT))
        }
    }

    @Test
    fun `test when updated timestamp is too fast - next element`() {
        val measurements = generate(5)
        val updatedMeasurement = measurements[3]
        updatedMeasurement.timestamp = measurements[4].timestamp

        try {
            updatedMeasurement.validateUpdatedMeasurement(measurements)
            fail()
        } catch (e: Throwable) {
            assertThat(e.message, equalTo(TIMESTAMP_TO_SHORT))
        }
    }

    @Test
    fun `test when updated value is too big - previous element`() {
        val measurements = generate(5)
        val updatedMeasurement = measurements[3]
        updatedMeasurement.value = measurements[2].value + 10.0

        try {
            updatedMeasurement.validateUpdatedMeasurement(measurements)
            fail()
        } catch (e: Throwable) {
            assertThat(e.message, equalTo(NEW_VALUE_BIGGER))
        }
    }

    @Test
    fun `test when updated value is too big - next element`() {
        val measurements = generate(5)
        val updatedMeasurement = measurements[3]
        updatedMeasurement.value = measurements[4].value + 10.0

        try {
            updatedMeasurement.validateUpdatedMeasurement(measurements)
            fail()
        } catch (e: Throwable) {
            assertThat(e.message, equalTo(NEW_VALUE_BIGGER))
        }
    }

    @Test
    fun `test when updated value is last value in the list`() {
        val measurements = generate(5)
        val updatedMeasurement = measurements.last()
        updatedMeasurement.value = 0.0
        updatedMeasurement.validateUpdatedMeasurement(measurements)
    }

    @Test
    fun `validate should fail if meter name is empty`() {
        val meter = Meter(uid = 1L, name = "", showNotification = false)
        val data = listOf(MeterWithMeasurements(meter = meter))
        assertFalse(data.isValid())
    }

    @Test
    fun `validate should fail if one of measurement is too big`() {
        val meter = Meter(uid = 1L, name = "", showNotification = false)
        val measurements = generate(5)

        val updatedMeasurement = measurements[3]
        updatedMeasurement.value = measurements[4].value + 10.0

        val data = listOf(MeterWithMeasurements(meter = meter, measurements = measurements))
        assertFalse(data.isValid())
    }

    @Test
    fun `validate should fail if one of measurement is too short`() {
        val meter = Meter(uid = 1L, name = "", showNotification = false)
        val measurements = generate(5)

        val updatedMeasurement = measurements[3]
        updatedMeasurement.timestamp = measurements[2].timestamp

        val data = listOf(MeterWithMeasurements(meter = meter, measurements = measurements))
        assertFalse(data.isValid())
    }

    private fun generate(count: Int, topUp: Double = 0.0): List<Measurement> {
        val mutableList = ArrayList<Measurement>(count)
        val now = System.currentTimeMillis()

        for (i in count downTo 1) {
            mutableList.add(
                Measurement(
                    id = i,
                    timestamp = now - TimeUnit.DAYS.toMillis(i.toLong()),
                    topUpValue = topUp,
                    meterId = 1,
                    value = i * 2.0
                )
            )
        }

        return mutableList.toList()
    }
}