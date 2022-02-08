package com.niekam.smartmeter.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.niekam.smartmeter.R


class HealthBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var daysLeft = -1
        set(value) {
            field = value
            updateColors()
        }

    init {
        updateColors()
    }

    private fun updateColors() {
        val background = when {
            daysLeft < 0 -> {
                R.drawable.grey_drawable
            }
            daysLeft <= 2 -> {
                R.drawable.red_drawable
            }

            daysLeft in (3..6) -> {
                R.drawable.orange_drawable
            }

            else -> {
                R.drawable.green_drawable
            }
        }

        setBackground(ContextCompat.getDrawable(context, background))
    }
}