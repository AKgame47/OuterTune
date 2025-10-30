package com.novatune.app.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.novatune.app.LocalPlayerConnection
import com.novatune.app.LocalPlayerAwareWindowInsets
import com.novatune.app.constants.DEFAULT_PLAYER_BACKGROUND
import com.novatune.app.constants.DarkMode
import com.novatune.app.constants.DarkModeKey
import com.novatune.app.constants.PlayerBackgroundStyle
import com.novatune.app.constants.PlayerBackgroundStyleKey
import com.novatune.app.ui.component.BottomSheetState
import com.novatune.app.ui.player.BottomSheetPlayer
import com.novatune.app.ui.player.QueueScreen
import com.novatune.app.utils.rememberEnumPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
//    scrollBehavior: TopAppBarScrollBehavior,
    playerBottomSheetState: BottomSheetState,
    modifier: Modifier,
) {
    val playerConnection = LocalPlayerConnection.current
    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = DEFAULT_PLAYER_BACKGROUND
    )

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val onBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.FOLLOW_THEME -> MaterialTheme.colorScheme.secondary
        else ->
            if (useDarkTheme)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        QueueScreen(
            playerBottomSheetState = playerBottomSheetState,
            onTerminate = {
                playerConnection?.service?.queueBoard?.detachedHead = false
            },
            navController = navController
        )
        BottomSheetPlayer(
            state = playerBottomSheetState,
            navController = navController
        )

        // Banner ad for the player screen; respects player-aware insets and auto-hides on failure
        com.novatune.app.ads.BannerAd(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
        )
    }
}
