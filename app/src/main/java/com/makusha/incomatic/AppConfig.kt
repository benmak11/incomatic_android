package com.makusha.incomatic

/**
 * Single source of truth for runtime configuration, direct port of
 * incomatic (iOS)'s AppConfig.swift. Production values come from
 * BuildConfig fields injected from gitignored local.properties.
 */
object AppConfig {
    /**
     * Newest tax year known to exist when this build shipped, used until the
     * backend's supported-years list has been read at least once.
     *
     * Deliberately *not* derived from the current date. The backend throws when
     * asked for a year it has no rule pack for, so rolling forward on January 1
     * would break every calculation until that year's pack was published.
     * Erring backwards costs one stale year; erring forwards costs the app.
     */
    const val FALLBACK_TAX_YEAR = 2026

    @Volatile
    private var cachedTaxYear: Int = FALLBACK_TAX_YEAR

    /**
     * Tax year every calculation runs against — single source of truth for the
     * request builder, bonus payout-date captions, and yearly outlook.
     *
     * Resolved from the backend's `defaultTaxYear` and persisted by
     * [com.makusha.incomatic.data.TaxYearPrefs]. Read synchronously because most
     * callers are Compose bodies and pure value math; the persisted value is
     * loaded before the shell renders and the refresh happens once at launch,
     * so it is stable for the session. Was a hardcoded 2025 until 2026-08-06,
     * which left Android a full tax year behind the deployed rule packs.
     */
    val taxYear: Int get() = cachedTaxYear

    /** Ignores non-positive values so a malformed payload can't wipe a good year. */
    fun cacheTaxYear(year: Int) {
        if (year > 0) cachedTaxYear = year
    }

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
