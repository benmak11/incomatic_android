package com.makusha.incomatic.net

import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object ApiClient {
    private const val HTTP_UPGRADE_REQUIRED = 426

    /** The 426 body is a handful of short fields; anything larger is not ours. */
    private const val MAX_ERROR_BODY = 4096L

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /** A malformed body still blocks the app, just without the backend's wording. */
    private fun parseUpgradeRequirement(body: String): UpgradeRequirement =
        runCatching { json.decodeFromString<UpgradeRequirement>(body) }
            .getOrElse { UpgradeRequirement() }

    /** Set once by AccountManager on init, read on every request. Null when signed out. */
    var sessionTokenProvider: () -> String? = { null }

    /**
     * `android/1.0.0`. The backend records this on every request and compares it
     * against its configured minimum, answering 426 when this build is too old.
     */
    private val clientHeader = "android/${BuildConfig.VERSION_NAME}"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val token = sessionTokenProvider()
                val builder = chain.request().newBuilder()
                    .addHeader("X-Incomatic-Client", clientHeader)
                if (token != null) {
                    builder.addHeader("Authorization", "Bearer $token")
                }
                val response = chain.proceed(builder.build())
                if (response.code == HTTP_UPGRADE_REQUIRED) {
                    // peekBody so the body stays readable by whoever called us:
                    // callers still throw their own errors, the upgrade screen
                    // just gets in front of the failure first.
                    UpgradeGate.record(parseUpgradeRequirement(response.peekBody(MAX_ERROR_BODY).string()))
                }
                response
            }
            if (BuildConfig.DEBUG) {
                // BASIC only — method, URL, response code. Never BODY/HEADERS: request
                // bodies carry salary figures and the Authorization header carries the
                // session token, neither of which may hit logs (same rule the backend
                // itself follows).
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
                )
            }
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("${AppConfig.apiBaseUrl}/")
            .client(okHttpClient)
            .build()
    }

    val salaryCalculatorService: SalaryCalculatorService by lazy {
        SalaryCalculatorService(retrofit.create(RawSalaryCalculatorApi::class.java), json)
    }
}
