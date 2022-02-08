package com.niekam.smartmeter.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import com.niekam.smartmeter.BuildConfig
import com.niekam.smartmeter.R
import com.niekam.smartmeter.base.di.detailsModule
import com.niekam.smartmeter.base.di.mainModule
import com.niekam.smartmeter.base.di.notificationModule
import com.niekam.smartmeter.reminder.CHANNEL_ID
import com.niekam.smartmeter.settings.CurrencyHolder
import com.niekam.smartmeter.settings.SYMBOL_PREF
import com.niekam.smartmeter.tools.ads.getAdPreferences
import com.niekam.smartmeter.tools.now
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class SmartMeterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CurrencyHolder.default = getCurrency()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@SmartMeterApp)
            modules(listOf(notificationModule, mainModule, detailsModule))
        }

        maybeCreateNotificationChannel()
        maybeEnableAds()
    }

    private fun maybeEnableAds() {
        val prefs = this.getAdPreferences()
        if (prefs.shouldShowAd()) {
            return
        }

        val firstUsage = prefs.firstUsageTimestamp()
        if (firstUsage == 0L) {
            prefs.saveFirstUsageTimestamp()
            return
        }

        val diff = now() - firstUsage
        if (TimeUnit.MILLISECONDS.toHours(diff) > 1) {
            prefs.enableAds()
        }
    }

    private fun maybeCreateNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    private fun getCurrency(): Currency {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString(SYMBOL_PREF, null).let {
            if (it.isNullOrEmpty()) {
                Currency.getInstance(Locale.getDefault())
            } else {
                Currency.getInstance(it)
            }
        }
    }
}

