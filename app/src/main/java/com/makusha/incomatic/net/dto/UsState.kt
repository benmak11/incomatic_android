package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsState(
    val code: String,
    val name: String,
)
