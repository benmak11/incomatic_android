package com.makusha.incomatic.net

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("v1/tax-years")
    suspend fun getTaxYears(@Query("country") country: String): ResponseBody

    @POST("v1/auth/google")
    suspend fun signInWithGoogle(@Body request: RequestBody): ResponseBody

    @GET("v1/calculations")
    suspend fun listCalculations(@Query("limit") limit: Int): ResponseBody

    @GET("v1/calculations/{id}")
    suspend fun getCalculation(@Path("id") id: String): ResponseBody

    @DELETE("v1/calculations/{id}")
    suspend fun deleteCalculation(@Path("id") id: String)

    @DELETE("v1/account")
    suspend fun deleteAccount()

    @GET("v1/stocks/search")
    suspend fun searchStocks(@Query("q") query: String): ResponseBody

    @GET("v1/stocks/quote/{symbol}")
    suspend fun quoteStock(@Path("symbol") symbol: String): ResponseBody

    @GET("v1/grants")
    suspend fun listGrants(): ResponseBody

    @POST("v1/grants")
    suspend fun createGrant(@Body request: RequestBody): ResponseBody

    @PUT("v1/grants/{id}")
    suspend fun updateGrant(@Path("id") id: String, @Body request: RequestBody): ResponseBody

    @DELETE("v1/grants/{id}")
    suspend fun deleteGrant(@Path("id") id: String)
}
