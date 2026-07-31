package com.makusha.incomatic.account

import kotlinx.serialization.Serializable

/** Account profile mirror of the backend's GoogleSignInResponse.user. */
@Serializable
data class AccountUser(
    val id: String,
    val displayName: String? = null,
) {
    val initials: String
        get() {
            val name = displayName?.takeIf { it.isNotEmpty() } ?: return "?"
            val letters = name.split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .mapNotNull { it.firstOrNull()?.toString() }
                .joinToString("")
            return letters.ifEmpty { "?" }.uppercase()
        }
}
