package com.makusha.incomatic.account

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.data.SessionStore
import com.makusha.incomatic.data.StoredSession
import com.makusha.incomatic.net.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns the signed-in state, the session token (in EncryptedSharedPreferences via
 * [SessionStore]), and the Google sign-in flow. [ApiClient.sessionTokenProvider]
 * is wired here on init, so every authenticated request across the app — including
 * POST /v1/calculate's auto-save-when-signed-in behavior — picks up the token
 * without any other class needing to know about auth at all.
 */
class AccountManager(application: Application) : AndroidViewModel(application) {
    private val store = SessionStore(application)
    private val api = ApiClient.salaryCalculatorService

    private val _currentUser = MutableStateFlow<AccountUser?>(null)
    val currentUser: StateFlow<AccountUser?> = _currentUser.asStateFlow()

    private val _sessionToken = MutableStateFlow<String?>(null)
    val sessionToken: StateFlow<String?> = _sessionToken.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isSignedIn: Boolean get() = _sessionToken.value != null

    init {
        store.load()?.let { stored ->
            _sessionToken.value = stored.token
            _currentUser.value = stored.user
        }
        ApiClient.sessionTokenProvider = { _sessionToken.value }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /** Runs the Credential Manager Google sign-in flow and exchanges the token with the backend. */
    suspend fun signInWithGoogle(context: Context) {
        if (_isSigningIn.value) return
        _isSigningIn.value = true
        _errorMessage.value = null
        try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(AppConfig.googleWebClientId)
                .setFilterByAuthorizedAccounts(false)
                .build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val credentialManager = CredentialManager.create(context)
            val credential = credentialManager.getCredential(context, request).credential

            if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                _errorMessage.value = "Unexpected credential type"
                return
            }
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)

            val response = api.signInWithGoogle(googleCredential.idToken, googleCredential.displayName)
            val user = AccountUser(response.user.id, response.user.displayName)
            store.save(StoredSession(response.sessionToken, user))
            _sessionToken.value = response.sessionToken
            _currentUser.value = user
        } catch (e: GetCredentialCancellationException) {
            // User dismissed the account picker — not an error.
        } catch (e: GetCredentialException) {
            _errorMessage.value = e.errorMessage?.toString() ?: "Sign-in failed"
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Sign-in failed"
        } finally {
            _isSigningIn.value = false
        }
    }

    fun signOut() {
        store.clear()
        _sessionToken.value = null
        _currentUser.value = null
        _errorMessage.value = null
    }

    /**
     * Permanently deletes the account server-side (calculations, grants, budget,
     * directory record), then clears the local session. Returns true on success.
     * Required by App Store Guideline 5.1.1(v) equivalent policy for apps that
     * create an account — same behavior as iOS's AccountManager.deleteAccount().
     */
    suspend fun deleteAccount(): Boolean {
        if (_isDeletingAccount.value || !isSignedIn) return false
        _isDeletingAccount.value = true
        _errorMessage.value = null
        return try {
            api.deleteAccount()
            signOut()
            true
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Couldn't delete account"
            false
        } finally {
            _isDeletingAccount.value = false
        }
    }
}
