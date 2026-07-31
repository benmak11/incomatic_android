@file:Suppress("DEPRECATION")

package com.makusha.incomatic.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.makusha.incomatic.account.AccountUser
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StoredSession(val token: String, val user: AccountUser)

/**
 * Holds the active session (token + user profile) in `EncryptedSharedPreferences`
 * (AES-256, Android-Keystore-backed) rather than plain DataStore — unlike every
 * other local store in this app (OnboardingPrefs, GrantsStore), this one holds a
 * live bearer credential. Direct analog of iOS's KeychainStore.
 *
 * `EncryptedSharedPreferences`/`MasterKey` are marked deprecated in
 * security-crypto 1.1.0 with no drop-in replacement shipped in the same
 * library — Google's guidance is to hand-roll Tink directly, which is a lot
 * of extra surface for one JSON blob. Suppressed deliberately rather than
 * falling back to unencrypted storage for a live bearer token; revisit if/when
 * Jetpack ships an actual successor API.
 */
class SessionStore(context: Context) {
    private val key = "session_token"
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "session_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun load(): StoredSession? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching { json.decodeFromString(StoredSession.serializer(), raw) }.getOrNull()
    }

    fun save(session: StoredSession) {
        prefs.edit().putString(key, json.encodeToString(StoredSession.serializer(), session)).apply()
    }

    fun clear() {
        prefs.edit().remove(key).apply()
    }
}
