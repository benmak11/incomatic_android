package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

/**
 * Mirrors the backend's RsuGrant.java field-for-field. Server-assigned ids
 * are only meaningful once grant sync exists (Phase 4-blocked); local-only
 * grants get a client-generated "local_" id (see data/GrantsStore.kt).
 */
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
