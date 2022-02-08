package com.niekam.smartmeter.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.niekam.smartmeter.base.BaseViewModel
import com.niekam.smartmeter.data.MeterDataModel
import com.niekam.smartmeter.data.model.Meter
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(private val dataModel: MeterDataModel) : BaseViewModel() {
    val meter: MutableLiveData<Meter> by lazy {
        MutableLiveData<Meter>()
    }

    fun addMeter(name: String, balance: Double?) = viewModelScope.launch {
        withContext(backgroundDispatcher) {
            dataModel.addMeter(name, balance)
        }
    }

    fun loadMeter(id: Long) = viewModelScope.launch {
        val task = async(backgroundDispatcher) {
            dataModel.getMeterById(id)
        }

        meter.postValue(task.await())
    }
}