package com.lifeflowpro.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gamification_stats")
data class GamificationEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val current_streak: Int = 0,
    val max_streak: Int = 0,
    val last_completed_date: Long = 0
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val icon: String,
    val unlocked: Boolean = false,
    val unlocked_at: Long? = null
)
