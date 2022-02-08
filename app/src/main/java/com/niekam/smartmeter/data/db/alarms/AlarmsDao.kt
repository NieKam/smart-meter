package com.niekam.smartmeter.data.db.alarms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.niekam.smartmeter.data.model.AlarmData

@Dao
abstract class AlarmsDao {
    @Query("SELECT * FROM alarms_table")
    abstract fun getAll(): List<AlarmData>

    @Insert
    abstract fun insert(alarmData: AlarmData): Long

    @Query("DELETE FROM alarms_table WHERE id = :id")
    abstract fun delete(id: Long): Int

    @Query("SELECT * FROM alarms_table WHERE id=:id")
    abstract fun getById(id: Long): AlarmData?
}