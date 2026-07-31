package com.makusha.incomatic.net.dto

import kotlinx.serialization.Serializable

/** Mirrors the backend's StockSymbol/StockSearchResponse/StockQuote.java field-for-field. */
@Serializable
data class StockSymbol(val symbol: String, val name: String)

@Serializable
data class StockSearchResponse(val items: List<StockSymbol>)

@Serializable
data class StockQuote(val symbol: String, val price: Double, val asOf: String)
