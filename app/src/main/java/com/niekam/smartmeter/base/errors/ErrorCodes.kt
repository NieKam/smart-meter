package com.niekam.smartmeter.base.errors

import androidx.annotation.StringRes
import com.niekam.smartmeter.R

const val TIMESTAMP_TO_SHORT = "timestamp_to_short"
const val NEW_VALUE_BIGGER = "new_value_bigger"
const val MALFORMED_JSON = "malformed_JSON"

@StringRes
fun Throwable.mapErrorToStringResource(): Int {
    return when (this.message) {
        TIMESTAMP_TO_SHORT -> R.string.tooShortTimestamp

        NEW_VALUE_BIGGER -> R.string.valueBigger

        MALFORMED_JSON -> R.string.malformedJsonError

        else -> R.string.unknown
    }
}

