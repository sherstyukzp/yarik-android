package com.bitchat.android.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bitchat.android.R
import com.bitchat.android.ui.debug.DebugSettingsSheet
import com.bitchat.android.ui.theme.ChatUiMode
import com.bitchat.android.ui.theme.ChatUiModeManager

private object YarikRoutes {
    const val CHATS = "chats"
    const val CHAT = "chat"
    const val SETTINGS = "settings"
}

@Composable
fun YarikMessenger(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val showAppInfo by viewModel.showAppInfo.collectAsStateWithLifecycle()
    val showMeshPeerListSheet by viewModel.showMeshPeerList.collectAsStateWithLifecycle()
    val showVerificationSheet by viewModel.showVerificationSheet.collectAsStateWithLifecycle()
    val showSecurityVerificationSheet by viewModel.showSecurityVerificationSheet.collectAsStateWithLifecycle()
    val privateChatSheetPeer by viewModel.privateChatSheetPeer.collectAsStateWithLifecycle()
    val showPasswordPrompt by viewModel.showPasswordPrompt.collectAsStateWithLifecycle()
    val passwordPromptChannel by viewModel.passwordPromptChannel.collectAsStateWithLifecycle()
    val legacyPrivateMediaConsent by viewModel.legacyPrivateMediaConsent.collectAsStateWithLifecycle()

    var showDebugSheet by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        ChatUiModeManager.set(context, ChatUiMode.Bubbles)
    }

    LaunchedEffect(showAppInfo) {
        if (showAppInfo) {
            viewModel.hideAppInfo()
            navController.navigate(YarikRoutes.SETTINGS)
        }
    }

    NavHost(
        navController = navController,
        startDestination = YarikRoutes.CHATS,
    ) {
        composable(YarikRoutes.CHATS) {
            ConversationListScreen(
                viewModel = viewModel,
                onOpenPublicChat = { navController.navigate(YarikRoutes.CHAT) },
                onOpenSettings = { navController.navigate(YarikRoutes.SETTINGS) },
            )
        }
        composable(YarikRoutes.CHAT) {
            ChatScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                includeGlobalSheets = false,
            )
        }
        composable(YarikRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onShowDebug = { showDebugSheet = true },
            )
        }
    }

    PasswordPromptDialog(
        show = showPasswordPrompt,
        channelName = passwordPromptChannel,
        passwordInput = passwordInput,
        onPasswordChange = { passwordInput = it },
        onConfirm = {
            val channel = passwordPromptChannel
            if (passwordInput.isNotEmpty() && channel != null) {
                val success = viewModel.joinChannel(channel, passwordInput)
                if (success) passwordInput = ""
            }
        },
        onDismiss = { passwordInput = "" },
    )

    if (showMeshPeerListSheet) {
        MeshPeerListSheet(
            isPresented = true,
            viewModel = viewModel,
            onDismiss = viewModel::hideMeshPeerList,
            onShowVerification = {
                viewModel.hideMeshPeerList()
                viewModel.showVerificationSheet(fromSidebar = true)
            },
        )
    }

    if (showVerificationSheet) {
        VerificationSheet(
            isPresented = true,
            onDismiss = viewModel::hideVerificationSheet,
            viewModel = viewModel,
        )
    }

    if (showSecurityVerificationSheet) {
        SecurityVerificationSheet(
            isPresented = true,
            onDismiss = viewModel::hideSecurityVerificationSheet,
            viewModel = viewModel,
        )
    }

    privateChatSheetPeer?.let { peerID ->
        PrivateChatSheet(
            isPresented = true,
            peerID = peerID,
            viewModel = viewModel,
            onDismiss = {
                viewModel.hidePrivateChatSheet()
                viewModel.endPrivateChat()
            },
        )
    }

    if (showDebugSheet) {
        DebugSettingsSheet(
            isPresented = true,
            onDismiss = { showDebugSheet = false },
            meshService = viewModel.meshService,
        )
    }

    legacyPrivateMediaConsent?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelLegacyPrivateMedia(request.requestId) },
            title = { Text(stringResource(R.string.private_media_legacy_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.private_media_legacy_body,
                        request.fileName,
                        request.recipientNickname,
                        request.warning,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.approveLegacyPrivateMedia(request.requestId) }) {
                    Text(stringResource(R.string.private_media_legacy_send_once))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelLegacyPrivateMedia(request.requestId) }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}
