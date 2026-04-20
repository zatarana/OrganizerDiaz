package com.lifeflowpro.app

import android.app.Application
import com.lifeflowpro.app.data.db.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LifeFlowApplication : Application() {
    @Inject lateinit var dbInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()
        com.lifeflowpro.app.worker.NotificationHelper.createChannels(this)
        dbInitializer.initialize()
    }
}