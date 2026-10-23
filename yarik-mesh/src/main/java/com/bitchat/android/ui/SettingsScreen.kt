package com.bitchat.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bitchat.android.R
import com.bitchat.android.features.voice.LiveVoicePreferences
import com.bitchat.android.net.ArtiTorManager
import com.bitchat.android.net.TorMode
import com.bitchat.android.net.TorPreferenceManager
import com.bitchat.android.nostr.NostrProofOfWork
import com.bitchat.android.nostr.PoWPreferenceManager
import com.bitchat.android.service.MeshForegroundService
import com.bitchat.android.service.MeshServicePreferences
import com.bitchat.android.ui.theme.ThemePreference
import com.bitchat.android.ui.theme.ThemePreferenceManager
import com.bitchat.android.wifiaware.WifiAwareController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    onShowDebug: () -> Unit,
) {
    val context = LocalContext.current
    val nickname by viewModel.nickname.collectAsState()
    val themePref by ThemePreferenceManager.themeFlow.collectAsState()
    var nicknameDraft by remember(nickname) { mutableStateOf(nickname) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var confirmWipe by remember { mutableStateOf(false) }

    val supportedLanguages = remember(context) {
        LanguagePreferenceManager.supportedLanguages(context)
    }
    var selectedLanguageTag by remember {
        mutableStateOf(LanguagePreferenceManager.currentLanguageTag())
    }

    LaunchedEffect(Unit) { PoWPreferenceManager.init(context) }
    val powEnabled by PoWPreferenceManager.powEnabled.collectAsState()
    val powDifficulty by PoWPreferenceManager.powDifficulty.collectAsState()
    var backgroundEnabled by remember {
        mutableStateOf(MeshServicePreferences.isBackgroundEnabled(true))
    }
    var liveVoiceEnabled by remember {
        mutableStateOf(LiveVoicePreferences.isEnabled(context))
    }
    val torProvider = remember { ArtiTorManager.getInstance() }
    var torMode by remember { mutableStateOf(TorPreferenceManager.get(context)) }
    val torStatus by torProvider.statusFlow.collectAsState()
    val torAvailable = remember { torProvider.isTorAvailable() }
    val wifiAwareEnabled by WifiAwareController.enabled.collectAsState()
    val wifiAwareSupported by WifiAwareController.supported.collectAsState()

    val themeOptions = listOf(
        ThemePreference.System to stringResource(R.string.about_system),
        ThemePreference.Light to stringResource(R.string.about_light),
        ThemePreference.Dark to stringResource(R.string.about_dark),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                SettingsSectionLabel(stringResource(R.string.settings_section_profile))
                OutlinedTextField(
                    value = nicknameDraft,
                    onValueChange = { nicknameDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(stringResource(R.string.nickname_hint)) },
                    singleLine = true,
                )
                TextButton(
                    onClick = { viewModel.setNickname(nicknameDraft.trim()) },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = nicknameDraft.trim().isNotEmpty() && nicknameDraft.trim() != nickname,
                ) {
                    Text(stringResource(R.string.save_nickname))
                }
            }

            item {
                SettingsSectionLabel(stringResource(R.string.about_section_theme))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    themeOptions.forEachIndexed { index, (preference, label) ->
                        SegmentedButton(
                            selected = themePref == preference,
                            onClick = { ThemePreferenceManager.set(context, preference) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = themeOptions.size,
                            ),
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            item {
                val selectedLanguageName = supportedLanguages
                    .firstOrNull { it.languageTag == selectedLanguageTag }
                    ?.endonym
                    ?: stringResource(R.string.about_system_default)
                ListItem(
                    headlineContent = { Text(stringResource(R.string.about_language)) },
                    supportingContent = { Text(selectedLanguageName) },
                    leadingContent = {
                        Icon(Icons.Filled.Language, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        TextButton(onClick = { showLanguagePicker = true }) {
                            Text(stringResource(R.string.change_language))
                        }
                    },
                )
                DropdownMenu(
                    expanded = showLanguagePicker,
                    onDismissRequest = { showLanguagePicker = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.about_system_default)) },
                        onClick = {
                            showLanguagePicker = false
                            selectedLanguageTag = ""
                            LanguagePreferenceManager.setLanguage("")
                        },
                    )
                    supportedLanguages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.endonym) },
                            onClick = {
                                showLanguagePicker = false
                                selectedLanguageTag = language.languageTag
                                LanguagePreferenceManager.setLanguage(language.languageTag)
                            },
                        )
                    }
                }
            }

            item {
                SettingsSectionLabel(stringResource(R.string.settings_section_network))
                SettingsSwitchRow(
                    icon = { Icon(Icons.Filled.Bluetooth, contentDescription = null) },
                    title = stringResource(R.string.about_background_title),
                    subtitle = stringResource(R.string.about_background_desc),
                    checked = backgroundEnabled,
                    onCheckedChange = { enabled ->
                        backgroundEnabled = enabled
                        MeshServicePreferences.setBackgroundEnabled(enabled)
                        if (enabled) {
                            MeshForegroundService.start(context)
                        } else {
                            MeshForegroundService.stop(context)
                        }
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                SettingsSwitchRow(
                    icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                    title = stringResource(R.string.live_voice_title),
                    subtitle = stringResource(R.string.live_voice_subtitle),
                    checked = liveVoiceEnabled,
                    onCheckedChange = { enabled ->
                        liveVoiceEnabled = enabled
                        LiveVoicePreferences.setEnabled(context, enabled)
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                SettingsSwitchRow(
                    icon = { Icon(Icons.Filled.Wifi, contentDescription = null) },
                    title = stringResource(R.string.wifi_aware_title),
                    subtitle = stringResource(R.string.wifi_aware_subtitle),
                    checked = wifiAwareEnabled,
                    onCheckedChange = WifiAwareController::setEnabled,
                    enabled = wifiAwareSupported,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                SettingsSwitchRow(
                    icon = { Icon(Icons.Filled.Security, contentDescription = null) },
                    title = stringResource(R.string.about_tor_title),
                    subtitle = if (torMode == TorMode.ON && torStatus.running) {
                        stringResource(R.string.about_tor_connected, torStatus.bootstrapPercent)
                    } else {
                        stringResource(R.string.about_tor_route)
                    },
                    checked = torMode == TorMode.ON,
                    onCheckedChange = { enabled ->
                        if (torAvailable) {
                            torMode = if (enabled) TorMode.ON else TorMode.OFF
                            TorPreferenceManager.set(context, torMode)
                        }
                    },
                    enabled = torAvailable,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                SettingsSwitchRow(
                    icon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                    title = stringResource(R.string.about_pow),
                    subtitle = stringResource(R.string.about_pow_tip),
                    checked = powEnabled,
                    onCheckedChange = { PoWPreferenceManager.setPowEnabled(it) },
                )
                if (powEnabled) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text(
                            text = stringResource(
                                R.string.about_difficulty_value,
                                powDifficulty,
                                NostrProofOfWork.estimateMiningTime(powDifficulty),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Slider(
                            value = powDifficulty.toFloat(),
                            onValueChange = { PoWPreferenceManager.setPowDifficulty(it.toInt()) },
                            valueRange = 0f..32f,
                            steps = 31,
                        )
                    }
                }
            }

            item {
                SettingsSectionLabel(stringResource(R.string.settings_section_privacy))
                ListItem(
                    headlineContent = { Text(stringResource(R.string.emergency_wipe)) },
                    supportingContent = { Text(stringResource(R.string.emergency_wipe_subtitle)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        TextButton(onClick = { confirmWipe = true }) {
                            Text(stringResource(R.string.emergency_wipe))
                        }
                    },
                )
            }

            item {
                SettingsSectionLabel(stringResource(R.string.about_section_about))
                AboutFeatureCard()
                AboutHowToUseSection()
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.about_debug_settings)) },
                    leadingContent = {
                        Icon(Icons.Filled.BugReport, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        TextButton(onClick = onShowDebug) {
                            Text(stringResource(R.string.about_debug_settings))
                        }
                    },
                )
                Text(
                    text = stringResource(R.string.about_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
    }

    if (confirmWipe) {
        AlertDialog(
            onDismissRequest = { confirmWipe = false },
            title = { Text(stringResource(R.string.emergency_wipe)) },
            text = { Text(stringResource(R.string.emergency_wipe_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmWipe = false
                        viewModel.panicClearAllData()
                    },
                ) {
                    Text(stringResource(R.string.emergency_wipe))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmWipe = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        },
    )
}
