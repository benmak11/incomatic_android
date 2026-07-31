package com.makusha.incomatic.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makusha.incomatic.calculator.CalculatorViewModel
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors

/**
 * Guided Notebook onboarding — ported from android-onboarding.jsx's
 * AndOnboarding. Writes straight into the shared [CalculatorViewModel]
 * (see the Phase 3 plan's "one shared form, not two" decision), so Review's
 * live number and the final Calculate call reuse Calculator's own code
 * paths unchanged. `wantsBonus`/`wantsCommission` are local, onboarding-only
 * branch flags — answering "No" clears the backing form field.
 */
@Composable
fun OnboardingFlow(viewModel: CalculatorViewModel, onComplete: () -> Unit) {
    val colors = incColors()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form = uiState.form

    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var bonusAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var commissionAnswer by rememberSaveable { mutableStateOf<String?>(null) }

    val steps = ONB_STEPS
    val step = steps[stepIndex]
    fun goTo(i: Int) {
        stepIndex = i.coerceIn(0, steps.lastIndex)
    }
    val jumpTo: (String) -> Unit = { id ->
        val i = steps.indexOfFirst { it.id == id }
        if (i >= 0) goTo(i)
    }

    BackHandler(enabled = stepIndex > 0) { goTo(stepIndex - 1) }

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        AndOnbTopBar()
        AndOnbRail(index = stepIndex, total = steps.size)

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = stepIndex,
                label = "onb-step",
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { idx ->
                val current = steps[idx]
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                    when (current) {
                        OnbStep.Greet -> {
                            AndOnbBubble("Hi — let's find your real paycheck.")
                            Text(
                                "A few quick questions and we'll build your take-home picture together — you can edit anything later.",
                                style = IncType.secondary,
                                color = colors.textDim,
                                modifier = Modifier.padding(horizontal = 26.dp),
                            )
                        }
                        OnbStep.Wage -> {
                            AndOnbBubble(current.question!!)
                            OnbWageBody(form, viewModel::updateForm)
                        }
                        OnbStep.PayFrequency -> {
                            AndOnbBubble(current.question!!)
                            OnbPayFrequencyBody(form, viewModel::updateForm)
                        }
                        OnbStep.Bonus -> {
                            AndOnbBubble(current.question!!)
                            OnbYesNoAmountBody(
                                wants = bonusAnswer?.let { it == "yes" },
                                onWantsChange = { yes ->
                                    bonusAnswer = if (yes) "yes" else "no"
                                    if (!yes) viewModel.updateForm { it.copy(bonus = "", bonusDate = "") }
                                },
                                amountLabel = "Bonus amount",
                                amountValue = form.bonus,
                                onAmountChange = { v -> viewModel.updateForm { it.copy(bonus = v) } },
                                dateValue = form.bonusDate,
                                onDateChange = { v -> viewModel.updateForm { it.copy(bonusDate = v) } },
                            )
                        }
                        OnbStep.Commission -> {
                            AndOnbBubble(current.question!!)
                            OnbYesNoAmountBody(
                                wants = commissionAnswer?.let { it == "yes" },
                                onWantsChange = { yes ->
                                    commissionAnswer = if (yes) "yes" else "no"
                                    if (!yes) viewModel.updateForm { it.copy(commission = "") }
                                },
                                amountLabel = "Commission (annual)",
                                amountValue = form.commission,
                                onAmountChange = { v -> viewModel.updateForm { it.copy(commission = v) } },
                            )
                        }
                        OnbStep.FilingStatusStep -> {
                            AndOnbBubble(current.question!!)
                            OnbFilingStatusBody(form, viewModel::updateForm)
                        }
                        OnbStep.StatePicker -> {
                            AndOnbBubble(current.question!!)
                            OnbStatePickerBody(form, uiState.usStates, viewModel::updateForm)
                        }
                        OnbStep.Benefits -> {
                            AndOnbBubble(current.question!!)
                            OnbBenefitsBody(form, viewModel::updateForm)
                            OnbOptionalHint()
                        }
                        OnbStep.Retirement -> {
                            AndOnbBubble(current.question!!)
                            OnbRetirementBody(form, viewModel::updateForm)
                            OnbOptionalHint()
                        }
                        OnbStep.Review -> {
                            OnbReview(form, uiState.usStates, onJumpTo = jumpTo)
                        }
                    }
                }
            }
        }

        if (step == OnbStep.Review && uiState.errorMessage != null) {
            Text(
                uiState.errorMessage.orEmpty(),
                style = IncType.secondary,
                color = colors.red,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (stepIndex > 0) {
                IncButton(
                    text = "Back",
                    onClick = { goTo(stepIndex - 1) },
                    variant = IncButtonVariant.OUTLINED,
                    fullWidth = false,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.width(72.dp),
                )
            }
            IncButton(
                text = when {
                    step == OnbStep.Greet -> "Get started"
                    step == OnbStep.Review && uiState.isLoading -> "Calculating…"
                    step == OnbStep.Review -> "Looks right — calculate"
                    else -> "Continue"
                },
                onClick = {
                    if (step == OnbStep.Review) {
                        viewModel.calculate { onComplete() }
                    } else {
                        goTo(stepIndex + 1)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
