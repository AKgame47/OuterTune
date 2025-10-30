/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.referral

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * ReferralManager handles referral system for user acquisition.
 *
 * Features:
 * - Generate unique referral codes for users
 * - Track referrals and reward referrer and referee
 * - Validate referral codes
 *
 * Anti-abuse:
 * - Prevent self-referral
 * - One-time application per user
 * - Local tracking; backend validation can be integrated
 */
object ReferralManager {
    private const val PREFS_NAME = "novatune_referral_prefs"
    private const val KEY_REFERRAL_CODE = "my_referral_code"
    private const val KEY_USED_REFERRAL_CODE = "used_referral_code"
    private const val KEY_REFERRAL_COUNT = "referral_count"
    private const val KEY_HAS_APPLIED_REFERRAL = "has_applied_referral"

    // Each successful install referral grants +2 Ad Coins to referrer
    const val REFERRAL_REWARD_COINS = com.novatune.app.rewards.RewardManager.COINS_REFERRAL_SUCCESS

    /**
     * Get or generate the user's unique referral code.
     */
    fun getReferralCode(context: Context): String {
        val prefs = getPreferences(context)
        var code = prefs.getString(KEY_REFERRAL_CODE, null)

        if (code == null) {
            code = generateReferralCode()
            prefs.edit().putString(KEY_REFERRAL_CODE, code).apply()
        }

        return code
    }

    /**
     * Apply a referral code (first-time user).
     * Grants the referee coins immediately.
     * Backend should validate and reward referrer server-side.
     */
    fun applyReferralCode(
        context: Context,
        referralCode: String,
        onResult: (Boolean) -> Unit
    ) {
        val prefs = getPreferences(context)

        if (prefs.getBoolean(KEY_HAS_APPLIED_REFERRAL, false)) {
            onResult(false)
            return
        }

        if (!isValidReferralCode(referralCode)) {
            onResult(false)
            return
        }

        val myCode = getReferralCode(context)
        if (referralCode.equals(myCode, ignoreCase = true)) {
            onResult(false)
            return
        }

        prefs.edit()
            .putString(KEY_USED_REFERRAL_CODE, referralCode)
            .putBoolean(KEY_HAS_APPLIED_REFERRAL, true)
            .apply()

        // Referee gets coins locally
        com.novatune.app.rewards.RewardManager.addCoins(
            context,
            com.novatune.app.rewards.RewardManager.COINS_REFERRAL_SUCCESS,
            "Applied referral code: $referralCode"
        )

        // Notify backend here if available to grant referrer reward

        onResult(true)
    }

    /**
     * Called when the backend verifies that someone used this user's code for a first-time install.
     * Locally increments count and grants referrer coins.
     */
    fun incrementReferralCount(context: Context) {
        val prefs = getPreferences(context)
        val count = prefs.getInt(KEY_REFERRAL_COUNT, 0)
        prefs.edit().putInt(KEY_REFERRAL_COUNT, count + 1).apply()

        // Grant referrer reward coins
        com.novatune.app.rewards.RewardManager.addCoins(
            context,
            com.novatune.app.rewards.RewardManager.COINS_REFERRAL_SUCCESS,
            "Referral success"
        )
    }

    fun hasAppliedReferral(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_HAS_APPLIED_REFERRAL, false)
    }

    fun getAppliedReferralCode(context: Context): String? {
        return getPreferences(context).getString(KEY_USED_REFERRAL_CODE, null)
    }

    fun getReferralCount(context: Context): Int {
        return getPreferences(context).getInt(KEY_REFERRAL_COUNT, 0)
    }

    private fun generateReferralCode(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()
        return uuid.substring(0, 6)
    }

    private fun isValidReferralCode(code: String): Boolean {
        return code.matches(Regex("^[A-Z0-9]{6}$"))
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
