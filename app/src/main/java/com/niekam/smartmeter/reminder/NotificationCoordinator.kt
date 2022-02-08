package com.niekam.smartmeter.reminder

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import com.niekam.smartmeter.R
import com.niekam.smartmeter.activity.MainActivity
import com.niekam.smartmeter.data.db.alarms.AlarmsRepo
import com.niekam.smartmeter.data.model.AlarmData
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.logic.computeRemainingFunds
import com.niekam.smartmeter.logic.computeRemainingTimeMs
import com.niekam.smartmeter.tools.now
import com.niekam.smartmeter.tools.timestampToDateAndTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "Meter_Channel"
const val METER_ID_KEY = "meter_id_key"
const val ALARM_DATA_ID_KEY = "alarm_data_id"

class NotificationCoordinator(private val context: Context, private val alarmsRepo: AlarmsRepo) {
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private val uiScope = CoroutineScope(Dispatchers.Main)

    fun rescheduleNotification(alarmData: AlarmData) {
        Timber.i("Reschedule alarm ${alarmData.id} for ${alarmData.name} at ${alarmData.triggerTime.timestampToDateAndTime()}")
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            alarmData.triggerTime,
            getAlarmIntent(alarmData.id)
        )
    }

    fun scheduleNotification(meter: Meter, measurements: List<Measurement>) = uiScope.launch {
        val triggerTime = measurements.computeRemainingTimeMs() - TimeUnit.DAYS.toMillis(1)

        if (triggerTime < now()) {
            Timber.e("Alarm in the past. Skip...")
            return@launch
        }

        val alarmsData = AlarmData(meter.uid, meter.name, measurements.computeRemainingFunds(), triggerTime)
        val alarmsDataId = withContext(Dispatchers.IO) {
            alarmsRepo.delete(meter.uid)
            alarmsRepo.newAlarm(alarmsData)
        }

        check(alarmsData.id == meter.uid)

        Timber.i("Schedule alarm $alarmsDataId at ${triggerTime.timestampToDateAndTime()}")
        val intent = getAlarmIntent(alarmsDataId)
        alarmManager.run {
            cancel(intent)
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                this,
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intent
            )
        }
        enableReceiver(true)
    }

    fun cancelSchedule(id: Long) = uiScope.launch {
        Timber.i("Cancel alarm")
        val rows = withContext(Dispatchers.IO) {
            alarmsRepo.delete(id)
        }

        Timber.i("Removed $rows")

        alarmManager.cancel(getAlarmIntent(id))
        enableReceiver(false)
    }

    private fun enableReceiver(isEnabled: Boolean) {
        Timber.i("Enable receiver: $isEnabled")
        val receiver = ComponentName(context, BootCompletedReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            if (isEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getAlarmIntent(id: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(ALARM_DATA_ID_KEY, id)
        }

        return PendingIntent.getBroadcast(context, id.toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}

fun Context.getMeterNotification(data: AlarmData): Notification {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(METER_ID_KEY, data.id)
    }
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(getString(R.string.notificationTitle))
        .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.notificationContent, data.name)))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()
}