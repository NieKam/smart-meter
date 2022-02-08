package com.niekam.smartmeter.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.niekam.smartmeter.data.db.alarms.AlarmsDao
import com.niekam.smartmeter.data.db.meter.MeterDao
import com.niekam.smartmeter.data.model.AlarmData
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter

@Database(entities = [Meter::class, Measurement::class, AlarmData::class], version = 1)
abstract class SmartMeterDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao
    abstract fun alarmsDao(): AlarmsDao
}