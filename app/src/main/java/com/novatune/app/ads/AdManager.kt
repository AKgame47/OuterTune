/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.ads

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color as AndroidColor
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdManager handles loading and displaying banner, native, and rewarded ads
 * using Google Mobile Ads SDK.
 *
 * Features:
 * - Banner ads for in-app placements
 * - Native ads for customized ad experiences
 * - Rewarded ads to grant user rewards (coins, features, etc.)
 *
 * Usage:
 * Initialize once in Application onCreate:
 *   AdManager.initialize(context)
 *
 * Load banner ad:
 *   AdManager.loadBannerAd(context, adView)
 *
 * Load and show rewarded ad:
 *   AdManager.loadRewardedAd(context, onAdLoaded = { ... }, onAdFailedToLoad = { ... })
 */
object AdManager {
    private const val PREFS_NAME = "novatune_ads_prefs"
    private const val KEY_ADS_ENABLED = "ads_enabled"
    private const val KEY_LAST_REWARDED_AD_TIME = "last_rewarded_ad_time"

    // Production AdMob IDs provided by user (App ID style as provided)
    // Note: Ad unit IDs typically use "/" not "~". If the provided IDs are app IDs,
    // configure per your AdMob console. We keep them here by request.
    const val ADMOB_APP_OR_UNIT_ID: String = "ca-app-pub-4437286732065224~7273498745"

    // Fallback test unit IDs (used when ADMOB_APP_OR_UNIT_ID looks like an app ID or for testing)
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"

    // Resolve actual unit IDs. If provided ID contains "~" treat as app id and use test units.
    private val BANNER_AD_UNIT_ID: String
        get() = if (ADMOB_APP_OR_UNIT_ID.contains("~")) TEST_BANNER else ADMOB_APP_OR_UNIT_ID
    private val REWARDED_AD_UNIT_ID: String
        get() = if (ADMOB_APP_OR_UNIT_ID.contains("~")) TEST_REWARDED else ADMOB_APP_OR_UNIT_ID
    private val NATIVE_AD_UNIT_ID: String
        get() = if (ADMOB_APP_OR_UNIT_ID.contains("~")) TEST_NATIVE else ADMOB_APP_OR_UNIT_ID

    private var rewardedAd: RewardedAd? = null

    /**
     * Initialize the Mobile Ads SDK.
     * Call this once in your Application class onCreate().
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context) { /* initialized */ }
        // Request configuration can be customized if needed
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                .build()
        )
    }

    /**
     * Check if ads are enabled for this user.
     */
    fun areAdsEnabled(context: Context): Boolean {
        // Disable ads automatically while Premium is active
        val premiumActive = try {
            com.novatune.app.rewards.RewardManager.isPremiumActive(context)
        } catch (_: Throwable) {
            false
        }
        if (premiumActive) return false

        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_ADS_ENABLED, true) // Enabled by default
    }

    /**
     * Enable or disable ads for the user.
     */
    fun setAdsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit()
            .putBoolean(KEY_ADS_ENABLED, enabled)
            .apply()
    }

    /**
     * Load a banner ad into the provided AdView asynchronously.
     * Automatically hides the view on failure.
     */
    fun loadBannerAd(context: Context, adView: AdView) {
        if (!areAdsEnabled(context)) {
            adView.visibility = View.GONE
            return
        }

        adView.adUnitId = BANNER_AD_UNIT_ID
        adView.adSize = AdSize.BANNER
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                adView.visibility = View.GONE
            }

            override fun onAdLoaded() {
                adView.visibility = View.VISIBLE
            }
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    /**
     * Creates and returns a banner container that auto-hides on load failure.
     */
    fun createBannerContainer(context: Context): FrameLayout {
        val container = FrameLayout(context)
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val adView = AdView(context)
        adView.setBackgroundColor(AndroidColor.TRANSPARENT)
        container.addView(
            adView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // stick to bottom inside the container
                gravity = android.view.Gravity.BOTTOM
            }
        )
        loadBannerAd(context, adView)
        return container
    }

    /**
     * Load a rewarded ad. Once loaded, it can be shown.
     *
     * @param onAdLoaded Callback invoked when ad is successfully loaded
     * @param onAdFailedToLoad Callback invoked when ad fails to load
     */
    fun loadRewardedAd(
        context: Context,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    ) {
        if (!areAdsEnabled(context)) {
            onAdFailedToLoad?.invoke(LoadAdError(0, "Ads disabled", "", null, null))
            return
        }

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    onAdFailedToLoad?.invoke(loadAdError)
                }
            }
        )
    }

    /**
     * Show the rewarded ad if it's loaded.
     *
     * @param activity The activity context
     * @param onUserEarnedReward Callback invoked when user earns the reward
     */
    fun showRewardedAd(
        activity: android.app.Activity,
        onUserEarnedReward: (Int) -> Unit
    ) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                onUserEarnedReward(rewardAmount)

                getPreferences(activity).edit()
                    .putLong(KEY_LAST_REWARDED_AD_TIME, System.currentTimeMillis())
                    .apply()

                rewardedAd = null
            }
        }
    }

    /**
     * Check if enough time has passed since last rewarded ad.
     * Default cooldown 30 minutes to prevent abuse.
     */
    fun canShowRewardedAd(context: Context, cooldownMinutes: Int = 30): Boolean {
        val prefs = getPreferences(context)
        val lastTime = prefs.getLong(KEY_LAST_REWARDED_AD_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val cooldownMillis = cooldownMinutes * 60 * 1000L

        return (currentTime - lastTime) >= cooldownMillis
    }

    /**
     * Load a native ad.
     * Native ads can be customized to match your app's UI.
     *
     * @param onAdLoaded Callback with the loaded NativeAd
     * @param onAdFailedToLoad Callback invoked on failure
     */
    fun loadNativeAd(
        context: Context,
        onAdLoaded: ((NativeAd) -> Unit)? = null,
        onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    ) {
        if (!areAdsEnabled(context)) {
            onAdFailedToLoad?.invoke(LoadAdError(0, "Ads disabled", "", null, null))
            return
        }

        val adLoader = AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                onAdLoaded?.invoke(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    onAdFailedToLoad?.invoke(loadAdError)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
