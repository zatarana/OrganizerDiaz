package com.lifeflowpro.app.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lifeflowpro.app.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskActionReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "COMPLETE_TASK") {
            val taskId = intent.getLongExtra("TASK_ID", -1L)
            if (taskId != -1L) {
                // Cancel notification
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(taskId.toInt())
                
                // Complete task in repository
                CoroutineScope(Dispatchers.IO).launch {
                    val task = repository.allTasks.first().find { it.id == taskId }
                    if (task != null) {
                        repository.update(task.copy(status = "CONCLUIDA", completed_at = System.currentTimeMillis()))
                    }
                }
            }
        }
    }
}
