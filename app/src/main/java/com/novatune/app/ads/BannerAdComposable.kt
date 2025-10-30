/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.ads

import android.view.View
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Compose Banner Ad which:
 * - Loads asynchronously
 * - Auto-hides on failure
 * - Respects theme (uses transparent background)
 * - Avoids layout distortion (wraps content)
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visible = remember { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme

    AndroidView(
        factory = {
            val adView = AdView(it)
            adView.adUnitId = AdManager.ADMOB_APP_OR_UNIT_ID // will resolve in AdManager
            adView.adSize = AdSize.BANNER
            adView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            adView.visibility = View.GONE
            adView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    adView.visibility = View.VISIBLE
                    visible.value = true
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    adView.visibility = View.GONE
                    visible.value = false
                }
            }
            AdManager.loadBannerAd(context, adView)
            adView
        },
        modifier = modifier
            .wrapContentHeight()
            .height(if (visible.value) 50.dp else 0.dp),
        update = { /* no-op */ }
    )

    LaunchedEffect(colorScheme) {
        // No-op, but forces recomposition when theme changes to ensure color conformity if needed
    }
}