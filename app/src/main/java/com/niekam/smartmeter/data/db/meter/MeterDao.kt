package com.niekam.smartmeter.data.db.meter

import androidx.lifecycle.LiveData
import androidx.room.*
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements

@Dao
abstract class MeterDao {
    @Query("SELECT * FROM meters_table ORDER by uid DESC")
    abstract fun getAllLiveData(): LiveData<List<MeterWithMeasurements>>

    @Insert
    abstract fun insertMeter(meter: Meter): Long

    @Transaction
    open fun insertMeterAndMeasurement(meter: Meter, measurement: Measurement) {
        insertMeter(meter).also { id ->
            measurement.meterId = id
            insertMeasurement(measurement)
        }
    }

    @Transaction
    open fun insertMeterAndMeasurements(meter: Meter, measurements: List<Measurement>) {
        insertMeter(meter)
        insertMeasurements(measurements)
    }

    @Update
    abstract fun updateMeter(meter: Meter)

    @Delete
    abstract fun deleteMeter(meter: Meter)

    @Query("SELECT * FROM meters_table WHERE uid=:meterId")
    abstract fun getMeter(meterId: Long): Meter

    /* Measurements */

    @Query("SELECT * FROM measurement WHERE meterId=:id ORDER by timestamp ASC")
    abstract fun getAllMeasurements(id: Long): LiveData<List<Measurement>>

    @Update
    abstract fun updateMeasurement(measurement: Measurement)

    @Insert
    abstract fun insertMeasurement(measurement: Measurement): Long

    @Insert
    abstract fun insertMeasurements(measurement: List<Measurement>)

    @Delete
    abstract fun deleteMeasurement(measurement: Measurement)

    /* Backup */

    @Query("SELECT * FROM meters_table ORDER by uid DESC")
    abstract fun getAll(): List<MeterWithMeasurements>

    @Query("DELETE FROM meters_table")
    abstract fun deleteAll()
}