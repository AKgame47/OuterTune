/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.ads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd

/**
 * Loads a NativeAd and displays it with rounded corners and slight elevation.
 * Auto-adjusts color scheme via MaterialTheme, with smooth fade-in animation.
 */
@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        var disposed = false
        AdManager.loadNativeAd(
            context,
            onAdLoaded = { ad ->
                if (!disposed) nativeAd = ad
            },
            onAdFailedToLoad = {
                nativeAd = null
            }
        )
        onDispose {
            disposed = true
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    AnimatedVisibility(
        visible = nativeAd != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        Card(
            modifier = modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = colors
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Minimal native ad rendering (headline only) to keep logic modular.
                // Apps can expand this to include media, icon, call-to-action, etc.
                nativeAd?.let { ad ->
                    ad.headline?.let {
                        Text(text = it, style = MaterialTheme.typography.titleMedium)
                    }
                    ad.body?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}