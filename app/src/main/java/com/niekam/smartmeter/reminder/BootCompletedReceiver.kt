package com.niekam.smartmeter.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.niekam.smartmeter.data.db.alarms.AlarmsRepo
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class BootCompletedReceiver : BroadcastReceiver(), CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val repo: AlarmsRepo by inject(AlarmsRepo::class.java)
    private val notificationCoordinator: NotificationCoordinator by inject(NotificationCoordinator::class.java)

    override fun onReceive(context: Context, intent: Intent?) {
        Timber.i("Received intent ${intent?.action}")
        if (intent?.action != "android.intent.action.BOOT_COMPLETED") {
            return
        }

        launch(Dispatchers.Main) {
            val allAlarms = withContext(Dispatchers.IO) {
                repo.getAll()
            }

            allAlarms.forEach {
                notificationCoordinator.rescheduleNotification(it)
            }
        }
    }
}

