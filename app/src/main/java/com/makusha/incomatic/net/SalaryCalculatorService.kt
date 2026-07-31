package com.makusha.incomatic.net

import com.makusha.incomatic.net.dto.CalculateRequest
import com.makusha.incomatic.net.dto.CalculateResponse
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
}
