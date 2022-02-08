package com.niekam.smartmeter.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.gms.ads.MobileAds
import com.jakewharton.rxbinding2.view.RxView
import com.niekam.smartmeter.sharing.PICK_FILE_REQUEST_CODE
import com.niekam.smartmeter.sharing.ShareHelper
import com.niekam.smartmeter.R
import com.niekam.smartmeter.base.errors.mapErrorToStringResource
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.databinding.ActivityMainBinding
import com.niekam.smartmeter.fragment.overview.MetersOverviewFragmentDirections
import com.niekam.smartmeter.reminder.METER_ID_KEY
import com.niekam.smartmeter.settings.SettingsActivity
import com.niekam.smartmeter.tools.ads.getAdPreferences
import com.niekam.smartmeter.tools.disablePositive
import com.niekam.smartmeter.tools.disablePositiveIfEmpty
import com.niekam.smartmeter.tools.editTextStringValue
import com.niekam.smartmeter.tools.moneyInputValue
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by inject()
    private val shareHelper: ShareHelper by inject()

    private lateinit var binding: ActivityMainBinding

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Error while importing data")
        binding.loading.hide()
        Toast.makeText(this, throwable.mapErrorToStringResource(), Toast.LENGTH_LONG).show()
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        RxView.clicks(binding.fab).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe { showAddNewMeterDialog() }

        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.metersOverviewFragment) {
                binding.fab.show()
            } else {
                binding.fab.hide()
            }
        }

        onNewIntent(intent)

        if (this.getAdPreferences().shouldShowAd()) {
            MobileAds.initialize(this) {}
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extras = intent?.extras
        if (extras?.containsKey(METER_ID_KEY) == true) {
            val meterObserver = Observer<Meter> { meter ->
                openDetailsFragment(meter)
            }

            viewModel.meter.observe(this, meterObserver)
            viewModel.loadMeter(extras.getLong(METER_ID_KEY))
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.meter.also {
            if (it.hasObservers()) {
                it.removeObservers(this)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_FILE_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }

        data?.data?.let {
            importData(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.action_export -> {
                shareHelper.shareAppBackup(activity = this)
                true
            }

            R.id.action_import -> {
                showWarningDialog()
                true
            }

            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openDetailsFragment(meter: Meter) {
        val action = MetersOverviewFragmentDirections.actionOpenDetails(meter)
        findNavController(R.id.nav_host_fragment).navigate(action)
    }

    private fun showAddNewMeterDialog() {
        MaterialDialog(this).show {
            title(R.string.newMeterTitle)
            customView(R.layout.new_meter_dialog_layout)
            disablePositiveIfEmpty(R.id.et_name)
            positiveButton(R.string.ok) { dialog ->
                dialog.getCustomView().let {
                    val title = it.editTextStringValue(R.id.et_name)
                    val balance = it.moneyInputValue(R.id.mi_initialValue)
                    viewModel.addMeter(title, balance)
                }
            }
            negativeButton(R.string.cancel)
        }
    }

    private fun showWarningDialog() {
        MaterialDialog(this).show {
            title(R.string.warning)
            message(R.string.import_warning_msg)
            positiveButton(R.string.ok) {
                startActivityForResult(shareHelper.getContentIntent(), PICK_FILE_REQUEST_CODE)
            }
            negativeButton(R.string.cancel) { }
            icon(R.drawable.ic_warning)
            disablePositive(2500)
        }
    }

    private fun importData(uri: Uri) = CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
        binding.loading.show()
        withContext(Dispatchers.IO) {
            shareHelper.importData(uri)
        }
        binding.loading.hide()
    }
}
