package com.niekam.smartmeter.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.niekam.smartmeter.data.db.alarms.AlarmsRepo
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


class AlarmReceiver : BroadcastReceiver(), KoinComponent, CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val repo: AlarmsRepo by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        launch(Dispatchers.Main) {
            Timber.i("Receive alarm intent")
            val id = intent?.getLongExtra(ALARM_DATA_ID_KEY, -1)
            check(id != null && id > 0)

            val alarmData = withContext(Dispatchers.IO) {
                repo.getAlarmById(id)
            }

            alarmData?.let {
                Timber.i("Show notification for ${it.name}")
                with(NotificationManagerCompat.from(context)) {
                    notify(it.id.toInt(), context.getMeterNotification(it))
                }
            }

            val count = withContext(Dispatchers.IO) {
                repo.delete(id)
            }

            Timber.i("Alarm deleted ? ${count == 1}")
        }
    }
}