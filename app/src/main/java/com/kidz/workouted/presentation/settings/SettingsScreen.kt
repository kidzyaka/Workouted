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
        onResetOnboarding = { viewModel.resetOnboarding() },
        onExportData = { viewModel.getExportJson() },
        onImportData = { viewModel.importFromJson(it) },
        onClearBackupState = { viewModel.clearBackupState() }
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onUpdateHeight: (String) -> Unit,
    onUpdateWeight: (String) -> Unit,
    onUpdateAge: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onResetOnboarding: () -> Unit,
    onExportData: suspend () -> String,
    onImportData: (String) -> Unit,
    onClearBackupState: () -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
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
                .padding(16.dp)
        ) {
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
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = stringResource(R.string.data_management)) {
                SettingsItem(
                    icon = Icons.Default.FileUpload,
                    title = stringResource(R.string.export_data),
                    subtitle = stringResource(R.string.export_data_desc),
                    onClick = { createDocumentLauncher.launch("workouted_backup_${System.currentTimeMillis()}.json") }
                )
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = stringResource(R.string.import_data),
                    subtitle = stringResource(R.string.import_data_desc),
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json")) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
            onResetOnboarding = {},
            onExportData = { "" },
            onImportData = {},
            onClearBackupState = {}
        )
    }
}
