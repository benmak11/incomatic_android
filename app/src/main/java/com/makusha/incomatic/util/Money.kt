package com.makusha.incomatic.util

/** "$1,234.56" — shared by the live-preview ribbon and Insights breakdown. */
fun formatMoney(amount: Double, cents: Boolean = true): String {
    val pattern = if (cents) "%,.2f" else "%,.0f"
    return "$" + pattern.format(amount)
}
