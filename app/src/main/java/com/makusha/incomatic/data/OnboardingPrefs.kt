package com.makusha.incomatic.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

/** First-run gate for the Guided Notebook onboarding flow — one boolean flag, no repository layer needed. */
class OnboardingPrefs(private val context: Context) {
    private val hasCompletedKey = booleanPreferencesKey("has_completed_onboarding")

    val hasCompletedOnboarding: Flow<Boolean> =
        context.onboardingDataStore.data.map { it[hasCompletedKey] ?: false }

    suspend fun setCompleted() {
        context.onboardingDataStore.edit { it[hasCompletedKey] = true }
    }
}
