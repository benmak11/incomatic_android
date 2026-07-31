package com.makusha.incomatic.net

import com.makusha.incomatic.net.dto.CalculateRequest
import com.makusha.incomatic.net.dto.CalculateResponse
import com.makusha.incomatic.net.dto.CalculationListResponse
import com.makusha.incomatic.net.dto.GoogleSignInRequest
import com.makusha.incomatic.net.dto.GoogleSignInResponse
import com.makusha.incomatic.net.dto.GrantListResponse
import com.makusha.incomatic.net.dto.RsuGrant
import com.makusha.incomatic.net.dto.SavedCalculationDetail
import com.makusha.incomatic.net.dto.StockQuote
import com.makusha.incomatic.net.dto.StockSearchResponse
import com.makusha.incomatic.net.dto.StockSymbol
import com.makusha.incomatic.net.dto.UsState
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Typed client, named to match incomatic (iOS)'s SalaryCalculatorService.swift.
 * Encodes/decodes explicitly via kotlinx.serialization rather than a Retrofit
 * converter factory — see [RawSalaryCalculatorApi]'s doc for why.
 */
class SalaryCalculatorService(
    private val api: RawSalaryCalculatorApi,
    private val json: Json,
) {
    private val jsonMediaType = "application/json".toMediaType()

    suspend fun calculate(request: CalculateRequest): CalculateResponse {
        val body = json.encodeToString(CalculateRequest.serializer(), request).toRequestBody(jsonMediaType)
        val responseBody = api.calculate(body)
        return json.decodeFromString(CalculateResponse.serializer(), responseBody.string())
    }

    suspend fun getUsStates(): List<UsState> {
        val responseBody = api.getUsStates()
        return json.decodeFromString(ListSerializer(UsState.serializer()), responseBody.string())
    }

    suspend fun signInWithGoogle(idToken: String, displayName: String?): GoogleSignInResponse {
        val body = json.encodeToString(GoogleSignInRequest.serializer(), GoogleSignInRequest(idToken, displayName))
            .toRequestBody(jsonMediaType)
        val responseBody = api.signInWithGoogle(body)
        return json.decodeFromString(GoogleSignInResponse.serializer(), responseBody.string())
    }

    suspend fun listCalculations(limit: Int = 50): CalculationListResponse {
        val responseBody = api.listCalculations(limit)
        return json.decodeFromString(CalculationListResponse.serializer(), responseBody.string())
    }

    suspend fun getCalculation(id: String): SavedCalculationDetail {
        val responseBody = api.getCalculation(id)
        return json.decodeFromString(SavedCalculationDetail.serializer(), responseBody.string())
    }

    suspend fun deleteCalculation(id: String) {
        api.deleteCalculation(id)
    }

    suspend fun deleteAccount() {
        api.deleteAccount()
    }

    suspend fun searchStocks(query: String): List<StockSymbol> {
        val responseBody = api.searchStocks(query)
        return json.decodeFromString(StockSearchResponse.serializer(), responseBody.string()).items
    }

    suspend fun quoteStock(symbol: String): StockQuote {
        val responseBody = api.quoteStock(symbol)
        return json.decodeFromString(StockQuote.serializer(), responseBody.string())
    }

    suspend fun listGrants(): List<RsuGrant> {
        val responseBody = api.listGrants()
        return json.decodeFromString(GrantListResponse.serializer(), responseBody.string()).items
    }

    suspend fun createGrant(grant: RsuGrant): RsuGrant {
        val body = json.encodeToString(RsuGrant.serializer(), grant).toRequestBody(jsonMediaType)
        val responseBody = api.createGrant(body)
        return json.decodeFromString(RsuGrant.serializer(), responseBody.string())
    }

    suspend fun updateGrant(grant: RsuGrant): RsuGrant {
        val id = requireNotNull(grant.id) { "updateGrant requires an id" }
        val body = json.encodeToString(RsuGrant.serializer(), grant).toRequestBody(jsonMediaType)
        val responseBody = api.updateGrant(id, body)
        return json.decodeFromString(RsuGrant.serializer(), responseBody.string())
    }

    suspend fun deleteGrant(id: String) {
        api.deleteGrant(id)
    }
}
