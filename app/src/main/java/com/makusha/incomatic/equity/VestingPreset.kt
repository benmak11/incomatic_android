package com.makusha.incomatic.equity

import com.makusha.incomatic.net.dto.RsuGrant

/** Schedule presets offered in the grant form. Raw values are the presetId strings persisted on the backend. */
enum class VestingPreset(val displayName: String, val terms: Triple<Int, Int, Int>?) {
    ANNUAL4("4-year annual (25/25/25/25)", Triple(48, 0, 12)),
    MONTHLY1CLIFF("4-year monthly, 1-year cliff", Triple(48, 12, 1)),
    QUARTERLY1CLIFF("4-year quarterly, 1-year cliff", Triple(48, 12, 3)),
    CUSTOM("Custom", null);

    val presetId: String
        get() = when (this) {
            ANNUAL4 -> "annual4"
            MONTHLY1CLIFF -> "monthly1cliff"
            QUARTERLY1CLIFF -> "quarterly1cliff"
            CUSTOM -> "custom"
        }

    companion object {
        fun fromPresetId(id: String?): VestingPreset? = entries.find { it.presetId == id }
    }
}

/** Compact label for list rows: "4-yr monthly · 1-yr cliff". */
fun RsuGrant.VestingSchedule.label(): String {
    val years = if (totalMonths % 12 == 0) "${totalMonths / 12}-yr" else "$totalMonths-mo"
    val cadence = when (freqMonths) {
        1 -> "monthly"
        3 -> "quarterly"
        12 -> "annual"
        else -> "every $freqMonths mo"
    }
    val cliff = if (cliffMonths > 0) {
        val cliffLabel = if (cliffMonths % 12 == 0) "${cliffMonths / 12}-yr" else "$cliffMonths-mo"
        " · $cliffLabel cliff"
    } else ""
    return "$years $cadence$cliff"
}
