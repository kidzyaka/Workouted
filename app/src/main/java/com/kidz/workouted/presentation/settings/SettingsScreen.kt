package com.kidz.workouted.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidz.workouted.R
import com.kidz.workouted.presentation.components.StaggeredEntranceItem
import com.kidz.workouted.ui.theme.WorkoutedTheme
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onUpdateHeight = { viewModel.updateHeight(it) },
        onUpdateWeight = { viewModel.updateWeight(it) },
        onUpdateAge = { viewModel.updateAge(it) },
        onUpdateLanguage = { code ->
            viewModel.updateLanguage(code)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
        },
        onUpdateUserColor = { viewModel.updateUserColor(it) },
        onResetOnboarding = { viewModel.resetOnboarding() },
        onExportData = { viewModel.getExportJson() },
        onImportData = { viewModel.importFromJson(it) },
        onClearBackupState = { viewModel.clearBackupState() },
        onLogin = { u, p -> viewModel.login(u, p) },
        onRegister = { u, p -> viewModel.register(u, p) },
        onLogout = { viewModel.logout() },
        onPushBackup = { viewModel.pushBackupToCloud() },
        onPullBackup = { viewModel.pullBackupFromCloud() }
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onUpdateHeight: (String) -> Unit,
    onUpdateWeight: (String) -> Unit,
    onUpdateAge: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateUserColor: (String) -> Unit,
    onResetOnboarding: () -> Unit,
    onExportData: suspend () -> String,
    onImportData: (String) -> Unit,
    onClearBackupState: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onLogout: () -> Unit,
    onPushBackup: () -> Unit,
    onPullBackup: () -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportErrorMsg = stringResource(R.string.export_error)
    val importSuccessMsg = stringResource(R.string.import_success)
    val importErrorMsg = stringResource(R.string.import_error)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = onExportData()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(json)
                        }
                    }
                    snackbarHostState.showSnackbar(exportSuccessMsg)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(exportErrorMsg)
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val json = InputStreamReader(inputStream).readText()
                        pendingImportJson = json
                        showImportConfirmDialog = true
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(importErrorMsg.format(e.message ?: "Unknown"))
                }
            }
        }
    }

    LaunchedEffect(uiState.backupSuccess, uiState.backupError) {
        if (uiState.backupSuccess == true) {
            snackbarHostState.showSnackbar(importSuccessMsg)
            onClearBackupState()
        } else if (uiState.backupSuccess == false) {
            snackbarHostState.showSnackbar(importErrorMsg.format(uiState.backupError ?: "Unknown error"))
            onClearBackupState()
        }
    }

    LaunchedEffect(uiState.authError) {
        if (uiState.authError != null) {
            snackbarHostState.showSnackbar("Auth Error: ${uiState.authError}")
            onClearBackupState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            StaggeredEntranceItem(index = 0) {
                Column {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.manage_profile),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }

            StaggeredEntranceItem(index = 1) {
                SettingsSection(title = stringResource(R.string.language_region)) {
                    val languageName = when (uiState.language) {
                        "ru" -> "Русский"
                        "es" -> "Español"
                        "zh" -> "中文"
                        else -> "English"
                    }
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.app_language),
                        subtitle = languageName,
                        onClick = { showLanguageDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.profile_color),
                        subtitle = uiState.userColor ?: "Default",
                        onClick = { showColorDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StaggeredEntranceItem(index = 2) {
                SettingsSection(title = stringResource(R.string.physical_parameters)) {
                    ParameterInput(
                        icon = Icons.Default.Straighten,
                        label = stringResource(R.string.height_cm),
                        value = uiState.height,
                        onValueChange = onUpdateHeight
                    )
                    ParameterInput(
                        icon = Icons.Default.MonitorWeight,
                        label = stringResource(R.string.weight_kg_label),
                        value = uiState.weight,
                        onValueChange = onUpdateWeight
                    )
                    ParameterInput(
                        icon = Icons.Default.Height,
                        label = stringResource(R.string.age),
                        value = uiState.age,
                        onValueChange = onUpdateAge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StaggeredEntranceItem(index = 3) {
                SettingsSection(title = stringResource(R.string.server_account_sync)) {
                    if (uiState.token.isNullOrEmpty()) {
                        SettingsItem(
                            icon = Icons.Default.Person,
                            title = stringResource(R.string.login_register),
                            subtitle = stringResource(R.string.connect_to_cloud),
                            onClick = { showAuthDialog = true }
                        )
                    } else {
                        SettingsItem(
                            icon = Icons.Default.PersonOutline,
                            title = stringResource(R.string.logged_in),
                            subtitle = stringResource(R.string.friend_code) + ": ${uiState.friendCode}",
                            onClick = null,
                            showChevron = false
                        )
                        SettingsItem(
                            icon = Icons.Default.CloudUpload,
                            title = stringResource(R.string.push_to_cloud),
                            subtitle = stringResource(R.string.backup_to_server),
                            onClick = onPushBackup
                        )
                        SettingsItem(
                            icon = Icons.Default.CloudDownload,
                            title = stringResource(R.string.pull_from_cloud),
                            subtitle = stringResource(R.string.restore_from_server),
                            onClick = onPullBackup
                        )
                        SettingsItem(
                            icon = Icons.Default.Logout,
                            title = stringResource(R.string.logout),
                            subtitle = stringResource(R.string.disconnect_account),
                            onClick = onLogout
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StaggeredEntranceItem(index = 4) {
                SettingsSection(title = stringResource(R.string.data_management)) {
                    SettingsItem(
                        icon = Icons.Default.FileUpload,
                        title = stringResource(R.string.export_data),
                        subtitle = stringResource(R.string.export_data_desc),
                        onClick = {
                            createDocumentLauncher.launch(
                                "workouted_backup_${System.currentTimeMillis()}.json"
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.FileDownload,
                        title = stringResource(R.string.import_data),
                        subtitle = stringResource(R.string.import_data_desc),
                        onClick = { openDocumentLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StaggeredEntranceItem(index = 4) {
                SettingsSection(title = stringResource(R.string.about_app)) {
                    SettingsItem(
                        title = stringResource(R.string.version),
                        subtitle = "1.0",
                        showChevron = false
                    )
                    SettingsItem(
                        title = stringResource(R.string.github_repo),
                        subtitle = "kidzyaka/Workouted",
                        onClick = { uriHandler.openUri("https://github.com/kidzyaka/Workouted") }
                    )

                    if (com.kidz.workouted.BuildConfig.DEBUG) {
                        SettingsItem(
                            title = stringResource(R.string.reset_onboarding),
                            subtitle = "Debug only",
                            onClick = onResetOnboarding
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = uiState.language,
            onLanguageSelected = { code ->
                onUpdateLanguage(code)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text(stringResource(R.string.import_confirm_title)) },
            text = { Text(stringResource(R.string.import_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportJson?.let { onImportData(it) }
                        showImportConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.import_data))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showColorDialog) {
        ColorSelectionDialog(
            currentColor = uiState.userColor ?: "#FF9800",
            onColorSelected = { 
                onUpdateUserColor(it)
                showColorDialog = false 
            },
            onDismiss = { showColorDialog = false }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            onLogin = { u, p -> 
                onLogin(u, p)
                showAuthDialog = false 
            },
            onRegister = { u, p -> 
                onRegister(u, p)
                showAuthDialog = false 
            },
            onDismiss = { showAuthDialog = false },
            isLoading = uiState.isLoading
        )
    }
}

@Composable
fun AuthDialog(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.server_account)) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = { onRegister(username, password) },
                    enabled = username.isNotBlank() && password.isNotBlank() && !isLoading
                ) {
                    Text(stringResource(R.string.action_register))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onLogin(username, password) },
                    enabled = username.isNotBlank() && password.isNotBlank() && !isLoading
                ) {
                    Text(stringResource(R.string.action_login))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ColorSelectionDialog(
    currentColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_profile_color)) },
        text = {
            // Using a simple column/row layout for the colors
            Column {
                for (i in colors.indices step 4) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (j in 0 until 4) {
                            if (i + j < colors.size) {
                                val colorHex = colors[i + j]
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable { onColorSelected(colorHex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(color = Color(android.graphics.Color.parseColor(colorHex)))
                                    }
                                    if (currentColor.equals(colorHex, ignoreCase = true)) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "ru" to "Русский",
        "es" to "Español",
        "zh" to "中文"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.app_language)) },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == currentLanguage,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = true
) {
    Surface(
        onClick = onClick ?: {},
        color = Color.Transparent,
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (showChevron) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun ParameterInput(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        
        var textValue by remember(value) { mutableStateOf(value) }
        
        OutlinedTextField(
            value = textValue,
            onValueChange = { 
                textValue = it
                onValueChange(it)
            },
            modifier = Modifier.width(80.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    WorkoutedTheme {
        SettingsContent(
            uiState = SettingsUiState(height = "180", weight = "85", age = "30", language = "en"),
            onUpdateHeight = {},
            onUpdateWeight = {},
            onUpdateAge = {},
            onUpdateLanguage = {},
            onUpdateUserColor = {},
            onResetOnboarding = {},
            onExportData = { "" },
            onImportData = {},
            onClearBackupState = {},
            onLogin = { _, _ -> },
            onRegister = { _, _ -> },
            onLogout = {},
            onPushBackup = {},
            onPullBackup = {}
        )
    }
}
