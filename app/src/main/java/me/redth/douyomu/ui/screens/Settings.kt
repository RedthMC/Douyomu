package me.redth.douyomu.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import me.redth.douyomu.R
import me.redth.douyomu.ui.Loading
import me.redth.douyomu.ui.withState


val Context.settings by preferencesDataStore(name = "settings")

enum class Theme(
    val translationId: Int
) {
    LIGHT(R.string.light),
    DARK(R.string.dark),
    SYSTEM(R.string.system),
}

val THEME = stringPreferencesKey("theme")
val VIBRATION = booleanPreferencesKey("vibration")

val Preferences.theme: Theme
    get() = when (this[THEME]) {
        "light" -> Theme.LIGHT
        "dark" -> Theme.DARK
        "system" -> Theme.SYSTEM
        else -> Theme.SYSTEM
    }

var MutablePreferences.theme: Theme
    get() = when (this[THEME]) {
        "light" -> Theme.LIGHT
        "dark" -> Theme.DARK
        "system" -> Theme.SYSTEM
        else -> Theme.SYSTEM
    }
    set(value) {
        this[THEME] = when (value) {
            Theme.LIGHT -> "light"
            Theme.DARK -> "dark"
            Theme.SYSTEM -> "system"
        }
    }

val Preferences.shouldVibrate: Boolean
    get() = this[VIBRATION] ?: true

var MutablePreferences.shouldVibrate: Boolean
    get() = this[VIBRATION] ?: true
    set(value) {
        this[VIBRATION] = value
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage() {
    val dataStore = LocalContext.current.settings
    val settings = dataStore.data.withState() ?: return Loading()
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // Theme Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.theme))

            var expand by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expand,
                onExpandedChange = { expand = it },
            ) {
                TextField(
                    value = stringResource(settings.theme.translationId),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = if (expand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expand,
                    onDismissRequest = { expand = false },
                ) {
                    Theme.entries.forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(stringResource(theme.translationId)) },
                            onClick = {
                                scope.launch {
                                    dataStore.edit { mutablePreferences ->
                                        mutablePreferences.theme = theme
                                    }
                                }
                                expand = false
                            }
                        )
                    }
                }
            }
        }

        // Vibration Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.vibration),
            )
            Switch(
                checked = settings.shouldVibrate,
                onCheckedChange = {
                    scope.launch {
                        dataStore.edit {
                            it.shouldVibrate = !it.shouldVibrate
                        }
                    }
                },
            )
        }
    }
}
