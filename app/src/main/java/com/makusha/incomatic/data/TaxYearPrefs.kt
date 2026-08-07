package com.makusha.incomatic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.taxYearDataStore by preferencesDataStore(name = "tax_year_prefs")

/**
 * Caches the backend's newest published tax year across launches, so a newly
 * published rule pack takes effect without shipping an app update.
 *
 * Same job as the iOS side's `incomatic.taxYear` UserDefaults key. Non-positive
 * values are ignored on both read and write so a malformed payload cannot wipe
 * a good cached year.
 */
class TaxYearPrefs(private val context: Context) {
    private val taxYearKey = intPreferencesKey("tax_year")

    val taxYear: Flow<Int?> =
        context.taxYearDataStore.data.map { prefs -> prefs[taxYearKey]?.takeIf { it > 0 } }

    suspend fun setTaxYear(year: Int) {
        if (year <= 0) return
        context.taxYearDataStore.edit { it[taxYearKey] = year }
    }
}
