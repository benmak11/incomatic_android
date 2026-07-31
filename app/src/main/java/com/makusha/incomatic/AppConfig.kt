package com.makusha.incomatic

/**
 * Single source of truth for runtime configuration, direct port of
 * incomatic (iOS)'s AppConfig.swift. Production values come from
 * BuildConfig fields injected from gitignored local.properties.
 */
object AppConfig {
    /** Tax year every calculation runs against — single source of truth for
     * the request builder, bonus payout-date captions, and yearly outlook. */
    const val TAX_YEAR = 2025

    /** The Android emulator's loopback alias for the host machine — the
     * Android twin of iOS's localhost override. */
    private const val LOCAL_BASE_URL = "http://10.0.2.2:8080"

    /**
     * Active backend base URL. No runtime toggle UI yet — there's no
     * settings/account surface until the Account phase, so USE_LOCAL_BACKEND
     * is a build-time flag only for now (set in local.properties).
     */
    val apiBaseUrl: String
        get() = if (BuildConfig.DEBUG && BuildConfig.USE_LOCAL_BACKEND) {
            LOCAL_BASE_URL
        } else {
            BuildConfig.API_BASE_URL_PROD
        }

    /** Web-type OAuth client id — the Credential Manager `serverClientId` and the
     * token audience the backend's `GOOGLE_AUDIENCE` checks against. Not a secret;
     * OAuth client ids are meant to be embedded in client apps. */
    val googleWebClientId: String get() = BuildConfig.GOOGLE_WEB_CLIENT_ID
}
