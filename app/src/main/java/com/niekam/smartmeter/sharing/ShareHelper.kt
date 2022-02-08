package com.niekam.smartmeter.sharing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.niekam.smartmeter.base.errors.MALFORMED_JSON
import com.niekam.smartmeter.data.db.meter.MeterDao
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import com.niekam.smartmeter.logic.isValid
import com.niekam.smartmeter.reminder.NotificationCoordinator
import com.niekam.smartmeter.tools.now
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.*
import java.util.*

private const val MIME_TYPE_JSON = "application/json"
private const val MIME_TYPE_OCTET = "application/octet-stream"
private const val FILE_AUTHORITY = "com.niekam.smartmeter.dbg.fileprovider"

const val PICK_FILE_REQUEST_CODE = 1000

class ShareHelper(
    private val appCtx: Context,
    private val meterDao: MeterDao,
    private val notificationCoordinator: NotificationCoordinator
) {
    private val gson = Gson()

    suspend fun importData(data: Uri) = coroutineScope {
        withContext(Dispatchers.IO) {
            val loadedData = loadFromJson(data)
            if (!loadedData.isValid()) {
                throw Throwable(MALFORMED_JSON)
            }

            meterDao.run {
                getAll()
                    .filter { it.meter.showNotification }
                    .forEach { notificationCoordinator.cancelSchedule(it.meter.uid) }
                deleteAll()
            }

            loadFromJson(data).forEach {
                meterDao.insertMeterAndMeasurements(it.meter, it.measurements)
                if (it.meter.showNotification) {
                    notificationCoordinator.scheduleNotification(it.meter, it.measurements)
                }
            }
        }
    }

    fun shareAppBackup(activity: Activity) = CoroutineScope(Dispatchers.Main).launch {
        val task = withContext(Dispatchers.IO) {
            getFileCacheUri()
        }

        task?.let { uri ->
            val shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setStream(uri)
                .intent
                .setAction(Intent.ACTION_SEND)
                .apply {
                    data = uri
                    type = MIME_TYPE_JSON
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            shareIntent.resolveActivity(activity.packageManager)?.let {
                activity.startActivity(shareIntent)
            }
        }
    }

    fun getContentIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(MIME_TYPE_JSON, MIME_TYPE_OCTET))
        }
    }

    private suspend fun loadFromJson(data: Uri): List<MeterWithMeasurements> = coroutineScope {
        return@coroutineScope try {
            val stream = appCtx.contentResolver.openInputStream(data)
            val reader = BufferedReader(InputStreamReader(requireNotNull(stream)))

            gson.fromJson(reader, object : TypeToken<List<MeterWithMeasurements>>() {}.type)
                    as List<MeterWithMeasurements>
        } catch (e: Exception) {
            Timber.e(e, "Cannot parse json")
            throw Throwable(MALFORMED_JSON)
        }
    }

    private suspend fun getFileCacheUri(): Uri? {
        val all = meterDao.getAll()
        val json = gson.toJson(all)

        Timber.i("Export $json")
        return saveToCache(json)
    }

    private fun Long.toBackupDate() =
        DateFormat.format("MM_dd_yy_hh:mm:ss", Date(this).time).toString()

    private fun saveToCache(json: String): Uri? {
        val file = File(appCtx.cacheDir, "SmartTopUp_" + now().toBackupDate() + ".json")
        return try {
            BufferedWriter(FileWriter(file)).run {
                write(json)
                close()
            }
            Timber.i("File saved at: ${file.absolutePath}")
            FileProvider.getUriForFile(appCtx, FILE_AUTHORITY, file)
        } catch (e: Exception) {
            Timber.e(e, "Cannot save file")
            null
        }
    }
}