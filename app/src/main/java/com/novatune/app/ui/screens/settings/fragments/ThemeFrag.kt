/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.novatune.app.ui.screens.settings.fragments

import android.os.Build
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatune.app.R
import com.novatune.app.constants.DEFAULT_PLAYER_BACKGROUND
import com.novatune.app.constants.DarkMode
import com.novatune.app.constants.DarkModeKey
import com.novatune.app.constants.DynamicThemeKey
import com.novatune.app.constants.PlayerBackgroundStyle
import com.novatune.app.constants.PlayerBackgroundStyleKey
import com.novatune.app.constants.PureBlackKey
import com.novatune.app.ui.component.EnumListPreference
import com.novatune.app.ui.component.SwitchPreference
import com.novatune.app.utils.rememberEnumPreference
import com.novatune.app.utils.rememberPreference

@Composable
fun ColumnScope.ThemeAppFrag() {
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)

    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)

    SwitchPreference(
        title = { Text(stringResource(R.string.enable_dynamic_theme)) },
        icon = { Icon(Icons.Rounded.Palette, null) },
        checked = dynamicTheme,
        onCheckedChange = onDynamicThemeChange
    )
    EnumListPreference(
        title = { Text(stringResource(R.string.dark_theme)) },
        icon = { Icon(Icons.Rounded.DarkMode, null) },
        selectedValue = darkMode,
        onValueSelected = onDarkModeChange,
        valueText = {
            when (it) {
                DarkMode.ON -> stringResource(R.string.dark_theme_on)
                DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
            }
        }
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.pure_black)) },
        icon = { Icon(Icons.Rounded.Contrast, null) },
        checked = pureBlack,
        onCheckedChange = onPureBlackChange
    )
}


@Composable
fun ColumnScope.ThemePlayerFrag() {
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = DEFAULT_PLAYER_BACKGROUND
    )
    val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
        it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    EnumListPreference(
        title = { Text(stringResource(R.string.player_background_style)) },
        icon = { Icon(Icons.Rounded.BlurOn, null) },
        selectedValue = playerBackground,
        onValueSelected = onPlayerBackgroundChange,
        valueText = {
            when (it) {
                PlayerBackgroundStyle.FOLLOW_THEME -> stringResource(R.string.player_background_default)
                PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.player_background_gradient)
                PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
            }
        },
        values = availableBackgroundStyles
    )
}

