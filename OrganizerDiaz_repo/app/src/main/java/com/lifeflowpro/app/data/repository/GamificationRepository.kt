package com.lifeflowpro.app.data.repository

import com.lifeflowpro.app.data.db.dao.AchievementDao
import com.lifeflowpro.app.data.db.dao.GamificationDao
import com.lifeflowpro.app.data.db.entities.AchievementEntity
import com.lifeflowpro.app.data.db.entities.GamificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepository @Inject constructor(
    private val gamificationDao: GamificationDao,
    private val achievementDao: AchievementDao
) {
    val stats: Flow<GamificationEntity?> = gamificationDao.getStats()
    val achievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    suspend fun updateStats(stats: GamificationEntity) = gamificationDao.updateStats(stats)

    suspend fun processTaskCompletion() {
        val currentStats = stats.firstOrNull() ?: GamificationEntity()
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastDay = currentStats.last_completed_date / (24 * 60 * 60 * 1000)

        val newStreak = when {
            today == lastDay -> currentStats.current_streak // Already completed today
            today == lastDay + 1 -> currentStats.current_streak + 1 // Consecutive day
            else -> 1 // Streak broken
        }

        val newMaxStreak = maxOf(newStreak, currentStats.max_streak)
        
        updateStats(currentStats.copy(
            xp = currentStats.xp + 10,
            current_streak = newStreak,
            max_streak = newMaxStreak,
            last_completed_date = System.currentTimeMillis()
        ))
    }
}
