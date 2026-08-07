package com.makusha.incomatic.shell

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makusha.incomatic.BuildConfig
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.incColors
import com.makusha.incomatic.net.UpgradeRequirement

private const val PLAY_MARKET_URI = "market://details?id=com.makusha.incomatic"
private const val PLAY_WEB_URL = "https://play.google.com/store/apps/details?id=com.makusha.incomatic"

/**
 * Shown when the backend refuses this build with a 426. Deliberately offers no
 * way back: the block is server-side and everything behind it would fail.
 */
@Composable
fun UpgradeRequiredScreen(requirement: UpgradeRequirement) {
    val colors = incColors()
    val context = LocalContext.current

    // Falls back to our own wording so the screen is never blank just because
    // the payload changed shape.
    val message = requirement.message?.takeIf { it.isNotBlank() }
        ?: ("This version of Incomatic is no longer supported. " +
                "Update to the latest version to continue.")

    val footnote = requirement.minimumVersion
        ?.takeIf { it.isNotBlank() }
        ?.let { "This build is version ${BuildConfig.VERSION_NAME}. Version $it or later is required." }
        ?: "This build is version ${BuildConfig.VERSION_NAME}."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.sageBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "↑",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.sage,
                )
            }

            Spacer(modifier = Modifier.size(24.dp))

            Text(
                text = "Time for an update",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = message,
                fontSize = 16.sp,
                color = colors.textDim,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IncButton(
                text = "Update Incomatic",
                onClick = { openPlayStore(context) },
                variant = IncButtonVariant.SOLID,
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = footnote,
                fontSize = 13.sp,
                color = colors.textMute,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Prefers the Play app, falling back to the web listing on devices without it
 * so the button is never a dead end.
 */
private fun openPlayStore(context: android.content.Context) {
    val market = Intent(Intent.ACTION_VIEW, PLAY_MARKET_URI.toUri())
    try {
        context.startActivity(market)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, PLAY_WEB_URL.toUri()))
    }
}
