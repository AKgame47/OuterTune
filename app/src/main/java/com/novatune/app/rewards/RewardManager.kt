/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.rewards

import android.content.Context
import android.content.SharedPreferences

/**
 * RewardManager handles the Ad Coin & Premium system.
 *
 * Rules:
 * - 1 watched reward ad = +1 Ad Coin
 * - Referral success = +2 Ad Coins (first-time install)
 * - 7 Ad Coins -> 1 day of ad-free Premium
 * - 12 Ad Coins -> 2 days of ad-free Premium
 *
 * Premium:
 * - Tracks start and expiry time (millis)
 * - Auto-resets when expired
 * - Provides days remaining, coin balance, and next target info
 *
 * Storage: SharedPreferences
 */
object RewardManager {
    private const val PREFS_NAME = "novatune_rewards_prefs"

    private const val KEY_AD_COINS = "ad_coins"
    private const val KEY_TOTAL_COINS_EARNED = "total_coins_earned"
    private const val KEY_TOTAL_COINS_SPENT = "total_coins_spent"

    private const val KEY_PREMIUM_START = "premium_start"
    private const val KEY_PREMIUM_EXPIRE = "premium_expire"

    // Coin amounts
    const val COINS_WATCH_AD = 1
    const val COINS_REFERRAL_SUCCESS = 2

    // Conversion thresholds
    private const val COINS_FOR_1_DAY = 7
    private const val COINS_FOR_2_DAYS = 12

    /**
     * Current coin balance.
     */
    fun getCoinBalance(context: Context): Int {
        return getPreferences(context).getInt(KEY_AD_COINS, 0)
    }

    /**
     * Add coins to balance and track total earned.
     */
    fun addCoins(context: Context, amount: Int, reason: String = "") {
        val prefs = getPreferences(context)
        val current = prefs.getInt(KEY_AD_COINS, 0)
        val totalEarned = prefs.getInt(KEY_TOTAL_COINS_EARNED, 0)
        prefs.edit()
            .putInt(KEY_AD_COINS, current + amount)
            .putInt(KEY_TOTAL_COINS_EARNED, totalEarned + amount)
            .apply()
    }

    /**
     * Spend coins if available; updates totals.
     */
    private fun spendCoins(context: Context, amount: Int): Boolean {
        val prefs = getPreferences(context)
        val current = prefs.getInt(KEY_AD_COINS, 0)
        if (current < amount) return false
        val totalSpent = prefs.getInt(KEY_TOTAL_COINS_SPENT, 0)
        prefs.edit()
            .putInt(KEY_AD_COINS, current - amount)
            .putInt(KEY_TOTAL_COINS_SPENT, totalSpent + amount)
            .apply()
        return true
    }

    /**
     * Grant premium for specified days and set start/expiry.
     */
    private fun grantPremiumDays(context: Context, days: Int) {
        val prefs = getPreferences(context)
        val start = System.currentTimeMillis()
        val expire = start + days * 24L * 60L * 60L * 1000L
        prefs.edit()
            .putLong(KEY_PREMIUM_START, start)
            .putLong(KEY_PREMIUM_EXPIRE, expire)
            .apply()
    }

    /**
     * Try redeeming 1-day premium using coins.
     */
    fun redeemOneDayPremium(context: Context): Boolean {
        if (!spendCoins(context, COINS_FOR_1_DAY)) return false
        grantPremiumDays(context, 1)
        return true
    }

    /**
     * Try redeeming 2-day premium using coins.
     */
    fun redeemTwoDaysPremium(context: Context): Boolean {
        if (!spendCoins(context, COINS_FOR_2_DAYS)) return false
        grantPremiumDays(context, 2)
        return true
    }

    /**
     * Returns true if premium is currently active, and auto-resets if expired.
     */
    fun isPremiumActive(context: Context): Boolean {
        val prefs = getPreferences(context)
        val expire = prefs.getLong(KEY_PREMIUM_EXPIRE, 0L)
        val now = System.currentTimeMillis()
        if (expire <= now) {
            if (expire != 0L) {
                // auto-reset timer
                prefs.edit()
                    .putLong(KEY_PREMIUM_START, 0L)
                    .putLong(KEY_PREMIUM_EXPIRE, 0L)
                    .apply()
            }
            return false
        }
        return true
    }

    /**
     * Days remaining for premium (integer, floor). 0 when not active.
     */
    fun getPremiumDaysRemaining(context: Context): Int {
        val prefs = getPreferences(context)
        val expire = prefs.getLong(KEY_PREMIUM_EXPIRE, 0L)
        val now = System.currentTimeMillis()
        if (expire <= now) return 0
        val millisRemaining = expire - now
        return (millisRemaining / (24L * 60L * 60L * 1000L)).toInt()
    }

    /**
     * Next target coin threshold given current balance.
     * Returns (targetCoins, targetDays). E.g. (7, 1) or (12, 2).
     */
    fun getNextTarget(context: Context): Pair<Int, Int> {
        val balance = getCoinBalance(context)
        return if (balance < COINS_FOR_1_DAY) {
            COINS_FOR_1_DAY to 1
        } else if (balance < COINS_FOR_2_DAYS) {
            COINS_FOR_2_DAYS to 2
        } else {
            COINS_FOR_1_DAY to 1 // default next small redemption
        }
    }

    /**
     * Reset all reward & premium data (for testing or user request).
     */
    fun resetAll(context: Context) {
        getPreferences(context).edit().clear().apply()
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
