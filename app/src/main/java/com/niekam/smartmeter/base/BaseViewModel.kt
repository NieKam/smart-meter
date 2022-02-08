package com.niekam.smartmeter.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import timber.log.Timber


abstract class BaseViewModel : ViewModel() {
    val errors = MutableLiveData<Throwable?>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                errors.value = throwable
            }
        }
    }

    var coroutineScope = viewModelScope + coroutineExceptionHandler
    var backgroundDispatcher = Dispatchers.IO

    open fun onAttached() {}

    open fun onDetached() {
        if (!errors.hasActiveObservers()) {
            errors.value = null
        }
    }
}