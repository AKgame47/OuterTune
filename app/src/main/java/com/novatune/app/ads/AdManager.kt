/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.ads

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
 *   AdManager.loadRewardedAd(context) { rewardAmount ->
 *       // Handle reward
 *   }
 */
object AdManager {
    private const val PREFS_NAME = "novatune_ads_prefs"
    private const val KEY_ADS_ENABLED = "ads_enabled"
    private const val KEY_LAST_REWARDED_AD_TIME = "last_rewarded_ad_time"
    
    // TODO: Replace with actual ad unit IDs from AdMob console
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test ID
    private const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110" // Test ID
    
    private var rewardedAd: RewardedAd? = null
    
    /**
     * Initialize the Mobile Ads SDK.
     * Call this once in your Application class onCreate().
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            // SDK initialized
        }
    }
    
    /**
     * Check if ads are enabled for this user.
     */
    fun areAdsEnabled(context: Context): Boolean {
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
     * Load a banner ad into the provided AdView.
     */
    fun loadBannerAd(context: Context, adView: AdView) {
        if (!areAdsEnabled(context)) return
        
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
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
                // User earned reward
                val rewardAmount = rewardItem.amount
                onUserEarnedReward(rewardAmount)
                
                // Save the timestamp of when user watched rewarded ad
                getPreferences(activity).edit()
                    .putLong(KEY_LAST_REWARDED_AD_TIME, System.currentTimeMillis())
                    .apply()
                
                // Reset to null after showing
                rewardedAd = null
            }
        }
    }
    
    /**
     * Check if enough time has passed since last rewarded ad.
     * This helps prevent abuse.
     * 
     * @param cooldownMinutes Cooldown period in minutes
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
