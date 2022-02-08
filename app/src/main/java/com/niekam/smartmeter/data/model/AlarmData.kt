package com.niekam.smartmeter.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "alarms_table")
@Parcelize
data class AlarmData(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val balance: Double,
    val triggerTime: Long
) : Parcelable