package com.lifeflowpro.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lifeflowpro.app.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TaskRepository
    @Inject lateinit var scheduler: TaskAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            runBlocking {
                val tasks = repository.allTasks.first()
                tasks.filter { it.status == "PENDENTE" && it.due_date != null }.forEach {
                    scheduler.schedule(it)
                }
            }
        }
    }
}
