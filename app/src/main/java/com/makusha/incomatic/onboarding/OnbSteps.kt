package com.makusha.incomatic.onboarding

/**
 * The Guided Notebook step list, ported from `onboarding-shared.jsx`'s
 * `ONB_STEPS` — same 10 steps, same order, same copy. The JS version is
 * generic over a `kind` string + a `field` key read via dynamic property
 * access; Kotlin has no equivalent that stays type-safe, so each step here
 * is its own concrete object/class instead — [OnbStepBody] pattern-matches
 * on the concrete type and writes to the right [com.makusha.incomatic.calculator.CalculatorState]
 * field directly.
 */
sealed class OnbStep(val id: String, val question: String? = null, val optional: Boolean = false) {
    object Greet : OnbStep("greet")
    object Wage : OnbStep("wage", "What's your annual or hourly wage?")
    object PayFrequency : OnbStep("payfreq", "How often do you get paid?")
    object Bonus : OnbStep("bonus", "Do you get a one-time bonus?")
    object Commission : OnbStep("commission", "Any commission on top of that?")
    object FilingStatusStep : OnbStep("filing", "What's your filing status?")
    object StatePicker : OnbStep("state", "Where do you work?")
    object Benefits : OnbStep("benefits", "Any pre-tax benefits deducted each period?", optional = true)
    object Retirement : OnbStep("retirement", "Setting anything aside for retirement?", optional = true)
    object Review : OnbStep("review")
}

val ONB_STEPS: List<OnbStep> = listOf(
    OnbStep.Greet,
    OnbStep.Wage,
    OnbStep.PayFrequency,
    OnbStep.Bonus,
    OnbStep.Commission,
    OnbStep.FilingStatusStep,
    OnbStep.StatePicker,
    OnbStep.Benefits,
    OnbStep.Retirement,
    OnbStep.Review,
)
