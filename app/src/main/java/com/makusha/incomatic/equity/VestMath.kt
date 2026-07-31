package com.makusha.incomatic.equity

import com.makusha.incomatic.net.dto.RsuGrant
import java.time.LocalDate

/**
 * Pure vest-distribution math — direct port of iOS's VestMath.swift (itself
 * ported from the Claude Design prototype's buildVestEvents/groupEventsByYear).
 * Shares vest proportionally to months covered: the cliff releases
 * cliffMonths/totalMonths of the grant at once, then a slice every
 * freqMonths until totalMonths. All valuation uses the grant's stored
 * pricePerShare — "today's price for every future vest".
 */
object VestMath {

    data class VestEvent(val date: LocalDate, val shares: Double, val isCliff: Boolean)

    data class YearGroup(
        val year: Int,
        val shares: Double,
        val value: Double,
        val vestCount: Int,
        val hasCliff: Boolean,
    )

    fun vestEvents(grant: RsuGrant): List<VestEvent> {
        val start = parseDate(grant.grantDate) ?: return emptyList()
        val total = maxOf(1, grant.schedule.totalMonths)
        val cliff = maxOf(0, minOf(grant.schedule.cliffMonths, total))
        val freq = maxOf(1, grant.schedule.freqMonths)
        val sharesPerMonth = grant.sharesTotal / total

        val events = mutableListOf<VestEvent>()
        var covered = cliff
        if (cliff > 0) {
            events += VestEvent(start.plusMonths(cliff.toLong()), sharesPerMonth * cliff, isCliff = true)
        }
        while (covered < total) {
            val next = minOf(covered + freq, total)
            events += VestEvent(start.plusMonths(next.toLong()), sharesPerMonth * (next - covered), isCliff = false)
            covered = next
        }
        return events
    }

    /** Year-level rollup of a single grant's events, ascending by year. */
    fun yearGroups(grant: RsuGrant): List<YearGroup> {
        val events = vestEvents(grant)
        return events.groupBy { it.date.year }
            .toSortedMap()
            .map { (year, group) ->
                val shares = group.sumOf { it.shares }
                YearGroup(
                    year = year,
                    shares = shares,
                    value = shares * grant.pricePerShare,
                    vestCount = group.size,
                    hasCliff = group.any { it.isCliff },
                )
            }
    }

    /** Dollar value vesting in [year] across all grants (each at its stored price). */
    fun value(year: Int, grants: List<RsuGrant>): Double =
        grants.sumOf { grant ->
            vestEvents(grant).filter { it.date.year == year }.sumOf { it.shares } * grant.pricePerShare
        }

    /** Next vest strictly after [after] across the grant's events. */
    fun nextVest(grant: RsuGrant, after: LocalDate = LocalDate.now()): VestEvent? =
        vestEvents(grant).firstOrNull { it.date > after }

    /** Last calendar year in which any grant still vests. Null when there are no grants. */
    fun finalVestYear(grants: List<RsuGrant>): Int? =
        grants.mapNotNull { vestEvents(it).lastOrNull()?.date?.year }.maxOrNull()

    fun parseDate(isoDate: String): LocalDate? = runCatching { LocalDate.parse(isoDate) }.getOrNull()
}
