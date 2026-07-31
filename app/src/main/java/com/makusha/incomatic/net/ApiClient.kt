package com.makusha.incomatic.net

import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /** Set once by AccountManager on init, read on every request. Null when signed out. */
    var sessionTokenProvider: () -> String? = { null }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val token = sessionTokenProvider()
                val request = if (token != null) {
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
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
