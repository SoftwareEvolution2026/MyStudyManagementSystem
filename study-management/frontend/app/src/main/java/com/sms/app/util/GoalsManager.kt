package com.sms.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GoalSettings(
    val dailyStudyGoalMinutes: Int = 120,
    val weeklyTaskGoal: Int = 5
)

class GoalsManager(private val context: Context) {

    companion object {
        private val DAILY_STUDY_GOAL_KEY = intPreferencesKey("daily_study_goal_minutes")
        private val WEEKLY_TASK_GOAL_KEY = intPreferencesKey("weekly_task_goal")
    }

    val goalsFlow: Flow<GoalSettings> = context.dataStore.data.map { prefs ->
        GoalSettings(
            dailyStudyGoalMinutes = prefs[DAILY_STUDY_GOAL_KEY] ?: 120,
            weeklyTaskGoal = prefs[WEEKLY_TASK_GOAL_KEY] ?: 5
        )
    }

    suspend fun saveGoals(dailyStudyGoalMinutes: Int, weeklyTaskGoal: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_STUDY_GOAL_KEY] = dailyStudyGoalMinutes
            prefs[WEEKLY_TASK_GOAL_KEY] = weeklyTaskGoal
        }
    }
}