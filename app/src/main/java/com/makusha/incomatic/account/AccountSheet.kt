package com.makusha.incomatic.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makusha.incomatic.design.IncButton
import com.makusha.incomatic.design.IncButtonVariant
import com.makusha.incomatic.design.IncType
import com.makusha.incomatic.design.incColors
import kotlinx.coroutines.launch

/**
 * Bottom sheet shown from the shell's account glyph — signed-out (sign-in CTA)
 * or signed-in (profile + saved count + sign-out/delete). Mirrors AccountSheet.swift.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(accountManager: AccountManager, savedCount: Int, onClose: () -> Unit) {
    val colors = incColors()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = colors.surface) {
        val user by accountManager.currentUser.collectAsStateWithLifecycle()
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp).padding(bottom = 28.dp)) {
            if (user != null) {
                SignedInState(accountManager, user, savedCount, onClose)
            } else {
                SignedOutState(accountManager, onClose)
            }
        }
    }
}

@Composable
private fun SignedOutState(accountManager: AccountManager, onClose: () -> Unit) {
    val colors = incColors()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isSigningIn by accountManager.isSigningIn.collectAsStateWithLifecycle()
    val errorMessage by accountManager.errorMessage.collectAsStateWithLifecycle()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(66.dp).clip(CircleShape).background(colors.sageBg),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", style = IncType.pageTitle.copy(fontSize = 28.sp), color = colors.sage)
        }
        Spacer(Modifier.size(16.dp))
        Text(
            "Save your calculations",
            style = IncType.sheetTitle.copy(fontSize = 26.sp),
            color = colors.text,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            "Sign in to keep your take-home projections in sync across your devices.",
            style = IncType.body,
            color = colors.textDim,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.size(22.dp))
        IncButton(
            text = if (isSigningIn) "Signing in…" else "Continue with Google",
            onClick = {
                scope.launch {
                    accountManager.signInWithGoogle(context)
                    if (accountManager.isSignedIn) onClose()
                }
            },
            variant = IncButtonVariant.OUTLINED,
            enabled = !isSigningIn,
        )
        Spacer(Modifier.size(16.dp))
        Text(
            "Signing in keeps your calculations tied to your account only.",
            style = IncType.secondary,
            color = colors.textMute,
        )
        if (errorMessage != null) {
            Spacer(Modifier.size(12.dp))
            Text(errorMessage.orEmpty(), style = IncType.secondary, color = colors.red)
        }
        Spacer(Modifier.size(14.dp))
        TextButton(onClick = onClose) {
            Text("Not now", style = IncType.body.copy(fontWeight = FontWeight.SemiBold), color = colors.textDim)
        }
    }
}

@Composable
private fun SignedInState(accountManager: AccountManager, user: AccountUser?, savedCount: Int, onClose: () -> Unit) {
    val colors = incColors()
    val scope = rememberCoroutineScope()
    val isDeletingAccount by accountManager.isDeletingAccount.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(colors.sage),
                contentAlignment = Alignment.Center,
            ) {
                Text(user?.initials ?: "?", style = IncType.title.copy(fontSize = 22.sp), color = colors.btnSolidText)
            }
            Column {
                Text(user?.displayName ?: "Signed in", style = IncType.sheetTitle.copy(fontSize = 24.sp), color = colors.text)
                Text("Signed in with Google", style = IncType.secondary.copy(fontWeight = FontWeight.SemiBold), color = colors.textDim)
            }
        }
        Spacer(Modifier.size(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Saved calculations", style = IncType.body.copy(fontWeight = FontWeight.SemiBold), color = colors.text)
                Text("Synced to your account", style = IncType.secondary, color = colors.textMute)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(colors.sageBg).padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(savedCount.toString(), style = IncType.body.copy(fontWeight = FontWeight.Bold), color = colors.sageDeep)
            }
        }
        Spacer(Modifier.size(22.dp))
        IncButton(
            text = "Sign Out",
            onClick = { accountManager.signOut(); onClose() },
            variant = IncButtonVariant.DANGER,
        )
        Spacer(Modifier.size(8.dp))
        IncButton(
            text = if (isDeletingAccount) "Deleting…" else "Delete Account",
            onClick = { confirmDelete = true },
            variant = IncButtonVariant.TEXT,
            enabled = !isDeletingAccount,
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete account?", style = IncType.title, color = colors.text) },
            text = {
                Text(
                    "This permanently deletes your account and every saved calculation. This can't be undone.",
                    style = IncType.secondary,
                    color = colors.textDim,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch { if (accountManager.deleteAccount()) onClose() }
                }) {
                    Text("Delete", color = colors.red)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = colors.text) }
            },
        )
    }
}
