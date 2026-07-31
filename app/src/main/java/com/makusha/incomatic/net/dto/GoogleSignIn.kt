package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

/** Mirrors the backend's GoogleSignInRequest.java — no nonce field, unlike Apple's flow. */
@Serializable
data class GoogleSignInRequest(
    val idToken: String,
    val displayName: String? = null,
)

/** Mirrors the backend's GoogleSignInResponse.java. */
@Serializable
data class GoogleSignInResponse(
    val sessionToken: String,
    val expiresAt: String,
    val user: User,
) {
    @Serializable
    data class User(
        val id: String,
        val displayName: String? = null,
    )
}
