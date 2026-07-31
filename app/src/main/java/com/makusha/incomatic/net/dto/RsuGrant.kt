package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

/** Mirrors the backend's RsuGrant.java field-for-field. Id is server-assigned; null on create. */
@Serializable
data class RsuGrant(
    val id: String? = null,
    val ticker: String? = null,
    val company: String? = null,
    val manualPrice: Boolean? = true,
    val sharesTotal: Double,
    val pricePerShare: Double,
    val grantDate: String,
    val schedule: VestingSchedule,
) {
    @Serializable
    data class VestingSchedule(
        val presetId: String? = null,
        val totalMonths: Int,
        val cliffMonths: Int,
        val freqMonths: Int,
    )
}

/** Mirrors the backend's GrantListResponse.java. */
@Serializable
data class GrantListResponse(val items: List<RsuGrant>)
