package com.niekam.smartmeter.tools

import android.os.Handler
import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.niekam.smartmeter.view.MoneyInput
import com.niekam.smartmeter.view.ObservableEditText


fun MaterialDialog.disablePositiveIfEmpty(@IdRes fieldId: Int): MaterialDialog {
    setActionButtonEnabled(WhichButton.POSITIVE, getCustomView().observableEditText(fieldId).textLength > 0)
    getCustomView().observableEditText(fieldId).observe { text ->
        setActionButtonEnabled(WhichButton.POSITIVE, text.trim().isNotEmpty())
    }
    return this
}

fun MaterialDialog.disablePositive(delayMs: Long): MaterialDialog {
    setActionButtonEnabled(WhichButton.POSITIVE, false)
    Handler().postDelayed({ setActionButtonEnabled(WhichButton.POSITIVE, true) }, delayMs)
    return this
}

fun View.observableEditText(@IdRes viewId: Int): ObservableEditText = findViewById(viewId)
fun View.editTextStringValue(@IdRes viewId: Int): String = findViewById<EditText>(viewId).text.toString()
fun EditText.editTextDoubleValue(): Double? {
    return this.text.toString().let {
        if (it.isEmpty()) null else it.toDouble()
    }
}

fun View.moneyInputValue(@IdRes viewId: Int): Double? = findViewById<MoneyInput>(viewId).moneyValue
fun View.moneyInputValueOrZero(@IdRes viewId: Int): Double = findViewById<MoneyInput>(viewId).moneyValue ?: 0.0

fun FloatingActionButton.hideButton(onHidden: (() -> Unit)) {
    this.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)
            onHidden.invoke()
        }
    })
}