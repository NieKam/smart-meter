package com.niekam.smartmeter.data.db.alarms

import com.niekam.smartmeter.data.model.AlarmData

class AlarmsRepo(private val alarmsDao: AlarmsDao) {

    suspend fun getAll(): List<AlarmData> {
        return alarmsDao.getAll()
    }

    suspend fun newAlarm(alarmData: AlarmData): Long {
        return alarmsDao.insert(alarmData)
    }

    suspend fun getAlarmById(id: Long): AlarmData? {
        return alarmsDao.getById(id)
    }

    suspend fun delete(id: Long): Int {
        return alarmsDao.delete(id)
    }
}