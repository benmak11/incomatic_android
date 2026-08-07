package com.makusha.incomatic.net

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * The backend's 426 payload. Every field is nullable because this arrives
 * precisely when the client is out of date, so it has to survive the backend
 * adding or renaming fields in a version this build has never seen.
 */
@Serializable
data class UpgradeRequirement(
    val message: String? = null,
    val minimumVersion: String? = null,
)

/**
 * Set once the backend refuses this build with a 426. Watched by `AppRoot`.
 *
 * A refusal is never cleared: the block is server-side and applies to every
 * endpoint, so there is nothing behind the upgrade screen that could work.
 */
object UpgradeGate {
    private val _requirement = MutableStateFlow<UpgradeRequirement?>(null)
    val requirement: StateFlow<UpgradeRequirement?> = _requirement.asStateFlow()

    /** First refusal wins; later ones carry the same verdict. */
    fun record(requirement: UpgradeRequirement) {
        _requirement.compareAndSet(null, requirement)
    }
}
