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
 * - Track referrals and reward both referrer and referee
 * - Validate referral codes
 * 
 * Usage:
 *   val myCode = ReferralManager.getReferralCode(context)
 *   ReferralManager.applyReferralCode(context, "ABC123") { success ->
 *       if (success) {
 *           // Reward granted
 *       }
 *   }
 */
object ReferralManager {
    private const val PREFS_NAME = "novatune_referral_prefs"
    private const val KEY_REFERRAL_CODE = "my_referral_code"
    private const val KEY_USED_REFERRAL_CODE = "used_referral_code"
    private const val KEY_REFERRAL_COUNT = "referral_count"
    private const val KEY_HAS_APPLIED_REFERRAL = "has_applied_referral"
    
    const val REFERRAL_REWARD = 50 // Reward amount for successful referral
    
    /**
     * Get or generate the user's unique referral code.
     * This code can be shared with friends.
     */
    fun getReferralCode(context: Context): String {
        val prefs = getPreferences(context)
        var code = prefs.getString(KEY_REFERRAL_CODE, null)
        
        if (code == null) {
            // Generate a unique referral code
            code = generateReferralCode()
            prefs.edit().putString(KEY_REFERRAL_CODE, code).apply()
        }
        
        return code
    }
    
    /**
     * Apply a referral code.
     * This should be called when a new user enters a referral code.
     * Can only be applied once per user.
     * 
     * @param referralCode The referral code to apply
     * @param onResult Callback with success status
     */
    fun applyReferralCode(
        context: Context,
        referralCode: String,
        onResult: (Boolean) -> Unit
    ) {
        val prefs = getPreferences(context)
        
        // Check if user has already applied a referral code
        if (prefs.getBoolean(KEY_HAS_APPLIED_REFERRAL, false)) {
            onResult(false)
            return
        }
        
        // Validate referral code format
        if (!isValidReferralCode(referralCode)) {
            onResult(false)
            return
        }
        
        // Cannot use own referral code
        val myCode = getReferralCode(context)
        if (referralCode.equals(myCode, ignoreCase = true)) {
            onResult(false)
            return
        }
        
        // In a real app, you would validate this with your backend server
        // For now, we'll accept any valid format code
        
        // Mark as applied
        prefs.edit()
            .putString(KEY_USED_REFERRAL_CODE, referralCode)
            .putBoolean(KEY_HAS_APPLIED_REFERRAL, true)
            .apply()
        
        // Grant reward to the new user
        com.novatune.app.rewards.RewardManager.addReward(
            context,
            REFERRAL_REWARD,
            "Applied referral code: $referralCode"
        )
        
        // In a real app, you would also notify the backend to reward the referrer
        
        onResult(true)
    }
    
    /**
     * Check if the user has already applied a referral code.
     */
    fun hasAppliedReferral(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_HAS_APPLIED_REFERRAL, false)
    }
    
    /**
     * Get the referral code that was applied by this user.
     */
    fun getAppliedReferralCode(context: Context): String? {
        return getPreferences(context).getString(KEY_USED_REFERRAL_CODE, null)
    }
    
    /**
     * Increment the referral count (called when someone uses this user's code).
     * In a real app, this would be called from the backend.
     */
    fun incrementReferralCount(context: Context) {
        val prefs = getPreferences(context)
        val count = prefs.getInt(KEY_REFERRAL_COUNT, 0)
        prefs.edit().putInt(KEY_REFERRAL_COUNT, count + 1).apply()
        
        // Grant reward for successful referral
        com.novatune.app.rewards.RewardManager.addReward(
            context,
            REFERRAL_REWARD,
            "Referral reward"
        )
    }
    
    /**
     * Get the number of successful referrals made by this user.
     */
    fun getReferralCount(context: Context): Int {
        return getPreferences(context).getInt(KEY_REFERRAL_COUNT, 0)
    }
    
    /**
     * Generate a unique referral code.
     * Format: 6 uppercase alphanumeric characters
     */
    private fun generateReferralCode(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()
        return uuid.substring(0, 6)
    }
    
    /**
     * Validate referral code format.
     */
    private fun isValidReferralCode(code: String): Boolean {
        return code.matches(Regex("^[A-Z0-9]{6}$"))
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
