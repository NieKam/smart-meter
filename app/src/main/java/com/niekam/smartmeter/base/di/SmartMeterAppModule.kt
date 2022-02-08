package com.niekam.smartmeter.base.di

import android.app.Application
import androidx.room.Room
import com.niekam.smartmeter.activity.MainActivityViewModel
import com.niekam.smartmeter.data.MeterDataModel
import com.niekam.smartmeter.data.MeterDataSource
import com.niekam.smartmeter.data.db.SmartMeterDatabase
import com.niekam.smartmeter.data.db.alarms.AlarmsRepo
import com.niekam.smartmeter.data.db.meter.MeasurementRepo
import com.niekam.smartmeter.data.db.meter.MeterRepo
import com.niekam.smartmeter.fragment.details.MeterDetailsViewModel
import com.niekam.smartmeter.fragment.overview.MetersOverviewViewModel
import com.niekam.smartmeter.reminder.NotificationCoordinator
import com.niekam.smartmeter.sharing.ShareHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val notificationModule = module {
    single { AlarmsRepo(get()) }
    single { get<SmartMeterDatabase>().alarmsDao() }
    factory { provideNotificationService(androidApplication(), get()) }
}

val mainModule = module {
    single { Room.databaseBuilder(get(), SmartMeterDatabase::class.java, "meter-db").build() }
    single { get<SmartMeterDatabase>().meterDao() }
    single { MeterRepo(get()) }
    single { MeasurementRepo(get()) }
    single<MeterDataModel> { MeterDataSource(get(), get()) }

    factory { ShareHelper(androidApplication(), get(), get()) }

    viewModel { MainActivityViewModel(get()) }
    viewModel { MetersOverviewViewModel(get()) }
}

val detailsModule = module {
    viewModel { MeterDetailsViewModel(get(), get()) }
}

private fun provideNotificationService(app: Application, alarmsRepo: AlarmsRepo): NotificationCoordinator =
    NotificationCoordinator(app, alarmsRepo)