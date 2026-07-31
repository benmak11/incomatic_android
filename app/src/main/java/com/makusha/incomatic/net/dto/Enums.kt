package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

@Serializable
enum class PayCadence(val periodsPerYear: Int) {
    ANNUAL(1), SEMIANNUAL(2), QUARTERLY(4), MONTHLY(12),
    SEMIMONTHLY(24), BIWEEKLY(26), WEEKLY(52), DAILY(260),
}

@Serializable
enum class FilingStatus {
    SINGLE, MARRIED, HEAD_OF_HOUSEHOLD
}

@Serializable
enum class Country {
    US, UK
}

@Serializable
enum class LineItemCategory {
    EARNINGS, TAX_FEDERAL, TAX_FICA, TAX_STATE, PRE_TAX_BENEFIT, RETIREMENT, POST_TAX, NET
}
