package com.niekam.smartmeter.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.niekam.smartmeter.R
import java.util.*

const val SYMBOL_PREF = "com.niekam.smartmeter.SYMBOL_PREF"

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.root_preferences)
    }

    override fun onResume() {
        super.onResume()
        registerChangeListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterChangeListener()
    }

    override fun onStart() {
        super.onStart()
        initCurrencySymbolPreference()
    }

    private fun initCurrencySymbolPreference() {
        val currencies = getAllCurrencies()
        findPreference<ListPreference>(SYMBOL_PREF)?.apply {
            value = value ?: getDefaultIfNotSet()
            entryValues = currencies.first
            entries = currencies.second
            summary = entry
        }
    }

    private fun getDefaultIfNotSet() = Currency.getInstance(Locale.getDefault()).currencyCode


    private fun getAllCurrencies(): Pair<Array<CharSequence>, Array<CharSequence>> {
        val currencyMap: List<Pair<String, String>> = Currency.getAvailableCurrencies()
            .sortedBy { c -> c.currencyCode }
            .map { c -> c.currencyCode to "${c.currencyCode} (${if (c.symbol == c.currencyCode) c.displayName else c.symbol})" }

        val codes: Array<CharSequence> = currencyMap.map { it.first }.toTypedArray()
        val currencies: Array<CharSequence> = currencyMap.map { it.second }.toTypedArray()
        return Pair(codes, currencies)
    }


    private fun updatePreference(key: String) {
        when (key) {
            SYMBOL_PREF -> {
                findPreference<ListPreference>(key)?.apply {
                    summary = entry
                }?.also {
                    CurrencyHolder.default = Currency.getInstance(it.value.toString())
                }
            }
        }
    }

    private fun registerChangeListener() {
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    private fun unregisterChangeListener() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        updatePreference(key)
    }
}