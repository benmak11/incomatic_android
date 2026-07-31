package com.makusha.incomatic.net

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Raw HTTP shape only — RequestBody/ResponseBody, not typed DTOs. The
 * jakewharton kotlinx.serialization Retrofit converter's Kotlin metadata
 * isn't readable by this project's Kotlin compiler version (its top-level
 * `create`/facade members resolved fine to javap but not to the Kotlin
 * compiler — a binary-metadata mismatch, not a naming issue), so
 * [SalaryCalculatorService] does the encode/decode explicitly instead of
 * relying on a Retrofit converter factory.
 */
interface RawSalaryCalculatorApi {
    @POST("v1/calculate")
    suspend fun calculate(@Body request: RequestBody): ResponseBody

    @GET("v1/countries/US/states")
    suspend fun getUsStates(): ResponseBody
}
