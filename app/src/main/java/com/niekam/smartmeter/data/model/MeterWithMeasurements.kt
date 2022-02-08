package com.niekam.smartmeter.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class MeterWithMeasurements(
    @Embedded val meter: Meter,
    @Relation(parentColumn = "uid", entityColumn = "meterId", entity = Measurement::class)
    var measurements: List<Measurement> = listOf()
)