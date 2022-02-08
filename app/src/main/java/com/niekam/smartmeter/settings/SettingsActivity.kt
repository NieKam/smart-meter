package com.niekam.smartmeter.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.niekam.smartmeter.BuildConfig
import com.niekam.smartmeter.R
import com.niekam.smartmeter.databinding.SettingsActivityBinding

private const val TAG = "SettingsFragment"

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, SettingsFragment(), TAG)
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tvVersion.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}