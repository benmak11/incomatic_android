package com.makusha.incomatic.util

/** "$1,234.56" — shared by the live-preview ribbon and Insights breakdown. */
fun formatMoney(amount: Double, cents: Boolean = true): String {
    val pattern = if (cents) "%,.2f" else "%,.0f"
    return "$" + pattern.format(amount)
}

/** "400" or "37.5" — whole number when integral, else one decimal. Shared by the vest timeline and grant rows. */
fun formatShares(shares: Double): String =
    if (shares % 1.0 == 0.0) shares.toInt().toString() else "%.1f".format(shares)

/**
 * Keep digits + at most one decimal point + at most 2 fractional digits.
 * Drops grouping commas and any other character so the result is Double-parseable.
 * Mirrors iOS's CalculatorFields.sanitizeCurrency.
 */
fun sanitizeCurrency(input: String): String {
    val result = StringBuilder()
    var sawDot = false
    var decimals = 0
    for (ch in input) {
        if (ch.isDigit()) {
            if (sawDot) {
                if (decimals >= 2) continue
                decimals++
            }
            result.append(ch)
        } else if (ch == '.' && !sawDot) {
            sawDot = true
            result.append(ch)
        }
    }
    return result.toString()
}

/**
 * Add thousands separators to the integer part of an already-sanitized raw
 * string, preserving a trailing "." or partial decimals mid-typing.
 * Mirrors iOS's CalculatorFields.groupCurrency.
 */
fun groupThousands(raw: String): String {
    if (raw.isEmpty()) return ""
    val dotIndex = raw.indexOf('.')
    val intPart = if (dotIndex >= 0) raw.substring(0, dotIndex) else raw
    val grouped = groupedInteger(intPart)
    return if (dotIndex >= 0) grouped + raw.substring(dotIndex) else grouped
}

private fun groupedInteger(digits: String): String {
    if (digits.isEmpty()) return digits
    val result = StringBuilder()
    val len = digits.length
    for (i in digits.indices) {
        if (i > 0 && (len - i) % 3 == 0) result.append(',')
        result.append(digits[i])
    }
    return result.toString()
}
