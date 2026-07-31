package com.makusha.incomatic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.makusha.incomatic.net.dto.RsuGrant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.grantsDataStore by preferencesDataStore(name = "grants_prefs")

/**
 * Local-only RSU grant persistence — no /v1/grants sync yet (that endpoint
 * 401s anonymous requests, and this app has no auth). Grants are stored as
 * one JSON blob; [RsuGrant] is already the backend's wire shape, so syncing
 * later is additive, not a rewrite.
 */
class GrantsStore(private val context: Context) {
    private val grantsKey = stringPreferencesKey("grants_json")
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(RsuGrant.serializer())

    val grants: Flow<List<RsuGrant>> = context.grantsDataStore.data.map { prefs ->
        prefs[grantsKey]?.let { raw -> runCatching { json.decodeFromString(serializer, raw) }.getOrNull() } ?: emptyList()
    }

    /** Create (null id) or update (existing id). Returns the saved grant. */
    suspend fun save(grant: RsuGrant): RsuGrant {
        val current = grants.first()
        val saved = if (grant.id == null) grant.copy(id = "local_" + UUID.randomUUID()) else grant
        val next = if (current.any { it.id == saved.id }) {
            current.map { if (it.id == saved.id) saved else it }
        } else {
            current + saved
        }
        write(next)
        return saved
    }

    suspend fun delete(id: String) {
        write(grants.first().filterNot { it.id == id })
    }

    private suspend fun write(grants: List<RsuGrant>) {
        context.grantsDataStore.edit { prefs -> prefs[grantsKey] = json.encodeToString(serializer, grants) }
    }
}
