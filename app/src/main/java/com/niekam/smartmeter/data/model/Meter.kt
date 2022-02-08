package com.niekam.smartmeter.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

const val INSUFFICIENT_MEASUREMENTS = -1L
const val NO_BALANCE = -1.0

@Parcelize
@Entity(tableName = "meters_table")
data class Meter(
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0,
    var name: String = "",
    var showNotification: Boolean = false
) : Parcelable