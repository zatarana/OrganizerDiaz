package com.lifeflowpro.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeflowpro.app.data.db.entities.TaskEntity
import com.lifeflowpro.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OverdueTaskWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: TaskRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tasks = repository.allTasks.first()
        val currentTime = System.currentTimeMillis()

        tasks.forEach { task ->
            if (task.status == "PENDENTE" && task.due_date != null && task.due_date < currentTime) {
                repository.update(task.copy(status = "ATRASADA"))
            }
        }

        return Result.success()
    }
}
