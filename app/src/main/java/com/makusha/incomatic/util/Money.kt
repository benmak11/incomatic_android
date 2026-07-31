package com.makusha.incomatic.util

/** "$1,234.56" — shared by the live-preview ribbon and Insights breakdown. */
fun formatMoney(amount: Double, cents: Boolean = true): String {
    val pattern = if (cents) "%,.2f" else "%,.0f"
    return "$" + pattern.format(amount)
}

/** "400" or "37.5" — whole number when integral, else one decimal. Shared by the vest timeline and grant rows. */
fun formatShares(shares: Double): String =
    if (shares % 1.0 == 0.0) shares.toInt().toString() else "%.1f".format(shares)
