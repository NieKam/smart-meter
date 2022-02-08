package com.niekam.smartmeter.tools.ads

import android.content.Context
import androidx.core.content.edit
import com.niekam.smartmeter.BuildConfig
import com.niekam.smartmeter.tools.now

fun Context.getAdPreferences(): AdsPreferences = AdsPreferences(this)

private const val AD_PREFS = "${BuildConfig.APPLICATION_ID}.AD_PREFS"
private const val SHOW_ADS = "${BuildConfig.APPLICATION_ID}.SHOW_ADS"
private const val FIRST_USAGE = "${BuildConfig.APPLICATION_ID}.FIRST_USAGE"

class AdsPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(AD_PREFS, Context.MODE_PRIVATE)

    fun shouldShowAd(): Boolean = prefs.getBoolean(SHOW_ADS, false)
    fun firstUsageTimestamp(): Long = prefs.getLong(FIRST_USAGE, 0)

    fun saveFirstUsageTimestamp() {
        prefs.edit { putLong(FIRST_USAGE, now()) }
    }

    fun enableAds() = prefs.edit { putBoolean(SHOW_ADS, true) }
}