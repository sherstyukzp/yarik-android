package com.bitchat.android.mesh

import android.app.Application
import com.bitchat.android.net.ArtiTorManager
import com.bitchat.android.nostr.RelayDirectory
import com.bitchat.android.ui.theme.ThemePreferenceManager

/**
 * Process-wide mesh and transport initialization (formerly [com.bitchat.android.BitchatApplication]).
 */
object MeshRuntime {
    fun start(application: Application) {
        PowerManager.getInstance(application).start()

        try {
            ArtiTorManager.getInstance().init(application)
        } catch (_: Exception) {
        }

        RelayDirectory.initialize(application)

        try {
            com.bitchat.android.nostr.LocationNotesInitializer.initialize(application)
        } catch (_: Exception) {
        }

        try {
            com.bitchat.android.favorites.FavoritesPersistenceService.initialize(application)
        } catch (_: Exception) {
        }

        try {
            com.bitchat.android.services.AppStateStore.initializeConversationPersistence(application)
        } catch (_: Exception) {
        }

        try {
            com.bitchat.android.nostr.NostrIdentityBridge.getCurrentNostrIdentity(application)
        } catch (_: Exception) {
        }

        ThemePreferenceManager.init(application)
        com.bitchat.android.ui.theme.ChatUiModeManager.init(application)

        try {
            com.bitchat.android.ui.debug.DebugPreferenceManager.init(application)
        } catch (_: Exception) {
        }

        try {
            val enabled = com.bitchat.android.ui.debug.DebugPreferenceManager.getWifiAwareEnabled(false)
            com.bitchat.android.wifiaware.WifiAwareController.initialize(application, enabled)
        } catch (_: Exception) {
        }

        try {
            com.bitchat.android.nostr.GeohashAliasRegistry.initialize(application)
            com.bitchat.android.nostr.GeohashConversationRegistry.initialize(application)
        } catch (_: Exception) {
        }

        try {
            com.bitchat.android.nostr.NostrBackgroundRuntime.initialize(application)
        } catch (_: Exception) {
        }

        try {
            com.bitchat.android.service.MeshServicePreferences.init(application)
        } catch (_: Exception) {
        }

        try {
            com.bitchat.android.service.MeshForegroundService.start(application)
        } catch (_: Exception) {
        }
    }
}
