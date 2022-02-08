package com.niekam.smartmeter.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Meter::class,
        parentColumns = ["uid"],
        childColumns = ["meterId"],
        onDelete = CASCADE
    )]
)
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var timestamp: Long,
    var value: Double,
    var topUpValue: Double,
    var meterId: Long = 0
)