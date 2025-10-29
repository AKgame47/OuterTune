/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.rewards

import android.content.Context
import android.content.SharedPreferences

/**
 * RewardManager handles user rewards system.
 * 
 * Features:
 * - Track user reward points/coins
 * - Grant rewards for actions (watching ads, referrals, etc.)
 * - Redeem rewards for premium features
 * 
 * Usage:
 *   RewardManager.addReward(context, 100, "Watched rewarded ad")
 *   val balance = RewardManager.getRewardBalance(context)
 *   RewardManager.redeemReward(context, 500, "Premium feature unlock")
 */
object RewardManager {
    private const val PREFS_NAME = "novatune_rewards_prefs"
    private const val KEY_REWARD_BALANCE = "reward_balance"
    private const val KEY_TOTAL_EARNED = "total_earned"
    private const val KEY_TOTAL_REDEEMED = "total_redeemed"
    
    // Reward amounts for different actions
    const val REWARD_WATCH_AD = 10
    const val REWARD_REFERRAL = 50
    const val REWARD_DAILY_LOGIN = 5
    
    /**
     * Get the current reward balance for the user.
     */
    fun getRewardBalance(context: Context): Int {
        return getPreferences(context).getInt(KEY_REWARD_BALANCE, 0)
    }
    
    /**
     * Add rewards to the user's balance.
     * 
     * @param amount Amount of rewards to add
     * @param reason Reason for the reward (for tracking)
     */
    fun addReward(context: Context, amount: Int, reason: String = "") {
        val prefs = getPreferences(context)
        val currentBalance = prefs.getInt(KEY_REWARD_BALANCE, 0)
        val totalEarned = prefs.getInt(KEY_TOTAL_EARNED, 0)
        
        prefs.edit()
            .putInt(KEY_REWARD_BALANCE, currentBalance + amount)
            .putInt(KEY_TOTAL_EARNED, totalEarned + amount)
            .apply()
    }
    
    /**
     * Redeem rewards from the user's balance.
     * 
     * @param amount Amount of rewards to redeem
     * @param reason Reason for redemption (for tracking)
     * @return true if redemption was successful, false if insufficient balance
     */
    fun redeemReward(context: Context, amount: Int, reason: String = ""): Boolean {
        val prefs = getPreferences(context)
        val currentBalance = prefs.getInt(KEY_REWARD_BALANCE, 0)
        
        if (currentBalance < amount) {
            return false
        }
        
        val totalRedeemed = prefs.getInt(KEY_TOTAL_REDEEMED, 0)
        
        prefs.edit()
            .putInt(KEY_REWARD_BALANCE, currentBalance - amount)
            .putInt(KEY_TOTAL_REDEEMED, totalRedeemed + amount)
            .apply()
        
        return true
    }
    
    /**
     * Get total rewards earned by the user (lifetime).
     */
    fun getTotalEarned(context: Context): Int {
        return getPreferences(context).getInt(KEY_TOTAL_EARNED, 0)
    }
    
    /**
     * Get total rewards redeemed by the user (lifetime).
     */
    fun getTotalRedeemed(context: Context): Int {
        return getPreferences(context).getInt(KEY_TOTAL_REDEEMED, 0)
    }
    
    /**
     * Check if user has enough rewards for a specific amount.
     */
    fun hasEnoughRewards(context: Context, amount: Int): Boolean {
        return getRewardBalance(context) >= amount
    }
    
    /**
     * Reset all reward data (for testing or user request).
     */
    fun resetRewards(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
