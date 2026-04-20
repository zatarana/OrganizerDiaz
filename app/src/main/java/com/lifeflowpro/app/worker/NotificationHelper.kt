package com.lifeflowpro.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lifeflowpro.app.MainActivity

object NotificationHelper {
    
    const val CHANNEL_TASKS = "tasks_channel"
    const val CHANNEL_FINANCE = "finance_channel"
    const val CHANNEL_DEBTS = "debts_channel"
    const val CHANNEL_GOALS = "goals_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(CHANNEL_TASKS, "Tarefas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notificações de tarefas e lembretes"
            }
            val financeChannel = NotificationChannel(CHANNEL_FINANCE, "Finanças", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alertas de contas a pagar, orçamentos limitados e receitas"
            }
            val debtChannel = NotificationChannel(CHANNEL_DEBTS, "Dívidas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Lembretes de vencimento de parcelas de dívidas"
            }
            val goalChannel = NotificationChannel(CHANNEL_GOALS, "Metas", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Progresso e alcance de metas financeiras"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(listOf(taskChannel, financeChannel, debtChannel, goalChannel))
        }
    }

    fun showNotification(
        context: Context, 
        channelId: String, 
        notificationId: Int, 
        title: String, 
        message: String, 
        actionIntent: Intent? = null,
        actionTitle: String? = null
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)

        if (actionIntent != null && actionTitle != null) {
            val pendingAction = PendingIntent.getBroadcast(
                context, 
                notificationId + 1000, 
                actionIntent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(android.R.drawable.ic_menu_edit, actionTitle, pendingAction)
        }

        manager.notify(notificationId, builder.build())
    }
}
