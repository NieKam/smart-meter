package com.niekam.smartmeter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.niekam.smartmeter.R
import com.niekam.smartmeter.settings.CurrencyHolder
import com.niekam.smartmeter.tools.editTextDoubleValue

class MoneyInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    private val etValue : ObservableEditText
    private val tvCurrency : TextView
    private val tilValue : TextInputLayout

    var moneyValue: Double? = 0.0
        get() = etValue.editTextDoubleValue()
        set(value) {
            etValue.setText(value.toString())
            field = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.money_input_view, this, true)
        etValue = findViewById(R.id.et_value)
        tvCurrency = findViewById(R.id.tv_currency)
        tilValue = findViewById(R.id.til_value)

        tvCurrency.text = CurrencyHolder.default.symbol

        val ta = context.obtainStyledAttributes(attrs, R.styleable.MoneyInput)
        try {
            tilValue.hint = ta.getString(R.styleable.MoneyInput_input_hint)
        } finally {
            ta.recycle()
        }
    }
}