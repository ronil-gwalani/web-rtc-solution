package com.renxo.user.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.utils.json
import com.renxo.user.utils.setLanguageChanges
import com.renxo.user.viewmodels.SettingsVM
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingsVM,
    restartHome: () -> Unit,
    finish: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    GetOneTimeBlock {
        viewModel.setLanguageList(getLanguages(context))
    }
    GetOneTimeBlock {
        viewModel.calculatorTypes.clear()
        viewModel.calculatorTypes.add(context.getString(R.string.floating))
        viewModel.calculatorTypes.add(context.getString(R.string.fixed))
    }

    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is SettingsVM.Finish -> {
                    if (event.value) {
                        val langCode = viewModel.selectedLanguage.code
                        setLanguageChanges(context, langCode) { orientation ->
                            viewModel.languageExpended = false
                            val window = (view.context as? Activity)?.window
                            window?.decorView?.layoutDirection = orientation
                            restartHome()
                        }
                    } else {
                        finish()
                    }
                }


            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
     ,   topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.whiteColor),
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = finish) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.toggle)
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.whiteColor
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    UserInfoRow(
                        icon = Icons.Default.Person,
                        label = stringResource(R.string.user_id),
                        value = viewModel.userId
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    UserInfoRow(
                        icon = Icons.Default.Info,
                        label = stringResource(R.string.device_id),
                        value = viewModel.deviceId
                    )
                }
            }

            // Settings Options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SettingsDropdown(
                        label = stringResource(R.string.language),
                        value = viewModel.selectedLanguage.value,
                        isExpanded = viewModel.languageExpended,
                        onExpandedChange = {
                            viewModel.languageExpended = !viewModel.languageExpended
                        },
                        items = viewModel.languageList.map { it.value },
                        onItemSelected = { selectedItem ->
                            val language =
                                viewModel.languageList.find { it.value == selectedItem }
                            language?.let { viewModel.updateLanguage(it) }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsDropdown(
                        label = stringResource(R.string.default_calculator),
                        value = if (viewModel.calculatorTypes.isNotEmpty()) {
                            if (viewModel.calculatorFloating) viewModel.calculatorTypes[0]
                            else viewModel.calculatorTypes[1]
                        } else "",
                        isExpanded = viewModel.calculatorIsExpanded,
                        onExpandedChange = {
                            viewModel.calculatorIsExpanded = !viewModel.calculatorIsExpanded
                        },
                        items = viewModel.calculatorTypes,
                        onItemSelected = { selectedItem ->
                            viewModel.updateCalculatorType(selectedItem == context.getString(R.string.floating))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsDropdown(
                        label = stringResource(R.string.default_equipment),
                        value = viewModel.selectedEquipment,
                        isExpanded = viewModel.defaultEquipmentIsExpanded,
                        onExpandedChange = {
                            viewModel.defaultEquipmentIsExpanded =
                                !viewModel.defaultEquipmentIsExpanded
                        },
                        items = viewModel.equipmentList,
                        onItemSelected = { selectedItem ->
                            viewModel.updateDefaultEquipment(selectedItem)
                        }
                    )

                }
            }

            Spacer(Modifier.weight(1f))
            // Save Button at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        // Launch in viewModelScope since saveAllChanges is a suspend function
                        viewModel.viewModelScope.launch {
                            viewModel.saveAllChanges()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

        }
    }
}

@Composable
private fun UserInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    label: String,
    value: String,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { onExpandedChange() },
            modifier = Modifier.width(200.dp)
        ) {
            TextField(
                value = value,
                onValueChange = { },
                readOnly = true,
                placeholder = { Text(stringResource(id = R.string.select)) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .height(56.dp),
                trailingIcon = {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.toggle)
                    )
                },
                colors = getTextFiledColors(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange() }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

@Serializable
data class Language(val code: String, val value: String)


private fun getLanguages(context: Context): List<Language> {
    val jsonString =
        context.resources.openRawResource(R.raw.languages).bufferedReader().use { it.readText() }
    return json.decodeFromString<List<Language>>(jsonString)

}

