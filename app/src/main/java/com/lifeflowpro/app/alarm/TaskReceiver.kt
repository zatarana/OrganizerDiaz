package com.lifeflowpro.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lifeflowpro.app.worker.NotificationHelper

class TaskReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Lembrete de Tarefa"

        if (taskId != -1L) {
            val actionIntent = Intent(context, TaskActionReceiver::class.java).apply {
                action = "COMPLETE_TASK"
                putExtra("TASK_ID", taskId)
            }

            NotificationHelper.showNotification(
                context = context,
                channelId = NotificationHelper.CHANNEL_TASKS,
                notificationId = taskId.toInt(),
                title = "Sua tarefa vence agora",
                message = taskTitle,
                actionIntent = actionIntent,
                actionTitle = "Concluir"
            )
        }
    }
}
