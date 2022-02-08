package com.niekam.smartmeter.dialog

import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.niekam.smartmeter.R
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.tools.disablePositiveIfEmpty
import com.niekam.smartmeter.tools.moneyInputValueOrZero
import com.niekam.smartmeter.tools.now
import com.niekam.smartmeter.tools.timestampToDateAndTime
import com.niekam.smartmeter.view.MoneyInput
import java.util.*

typealias ResultsListener = ((Measurement) -> Unit)?

class NewMeasurementDialog(private val rootView: View, private val measurement: Measurement? = null) {
    private var now = now()
    private var dialog: MaterialDialog? = null
    private val isEditMode = measurement != null
    private val show24HoursFormat = DateFormat.is24HourFormat(rootView.context)

    fun show(listener: ResultsListener) {
        dialog = MaterialDialog(rootView.context).show {
            customView(R.layout.new_measurement_dialog_layout).also {
                initCustomView(it.getCustomView())
            }
            disablePositiveIfEmpty(R.id.et_value)
            negativeButton(R.string.cancel)
            positiveButton(if (isEditMode) R.string.update else R.string.add) {
                it.getCustomView().let { v ->
                    val balance = v.moneyInputValueOrZero(R.id.mi_value)
                    val topUp = v.moneyInputValueOrZero(R.id.mi_topup)

                    if (isEditMode) {
                        measurement?.apply {
                            timestamp = now
                            value = balance
                            topUpValue = topUp
                        }?.also { it ->
                            listener?.invoke(it)
                        }
                    } else {
                        listener?.invoke(Measurement(timestamp = now, value = balance, topUpValue = topUp))
                    }
                }
            }
        }
    }

    private fun initCustomView(customView: View) {
        if (!isEditMode) {
            customView.getTimeView().apply {
                text = now.timestampToDateAndTime()
                setOnClickListener { showDatePicker() }
            }

            return
        }

        measurement?.let { m ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = m.timestamp
            }
            customView.apply {
                getTimeView().apply {
                    text = m.timestamp.timestampToDateAndTime()
                    setOnClickListener { showDatePicker(calendar) }
                }
                setMoneyValue(R.id.mi_value, m.value)
                setMoneyValue(R.id.mi_topup, m.topUpValue)
            }
        }
    }

    private fun View.setMoneyValue(@IdRes id: Int, value: Double) {
        this.findViewById<MoneyInput>(id).moneyValue = value
    }

    private fun View.getTimeView() = this.findViewById<TextView>(R.id.tv_time)
    private fun View.getWarningView() = this.findViewById<TextView>(R.id.tv_futureTimeWarning)

    private fun showDatePicker(calendar: Calendar = Calendar.getInstance()) {
        MaterialDialog(rootView.context).show {
            dateTimePicker(
                requireFutureDateTime = false,
                currentDateTime = calendar,
                show24HoursView = show24HoursFormat
            ) { _, dateTime ->
                now = dateTime.timeInMillis
                dialog?.getCustomView()?.let {
                    if (now >= now()) {
                        now = now()
                        it.getWarningView().visibility = View.VISIBLE
                    } else {
                        it.getWarningView().visibility = View.GONE
                    }

                    it.getTimeView().text = now.timestampToDateAndTime()
                }
            }
        }
    }
}