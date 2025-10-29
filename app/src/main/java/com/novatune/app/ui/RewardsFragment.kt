/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novatune.app.R
import com.novatune.app.ads.AdManager
import com.novatune.app.referral.ReferralManager
import com.novatune.app.rewards.RewardManager

/**
 * RewardsFragment displays the rewards and referral system UI.
 * 
 * Features:
 * - Display current reward balance
 * - Show referral code and allow entering others' codes
 * - Watch rewarded ads to earn points
 * - Redeem rewards for premium features
 */
@Composable
fun RewardsFragment(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var rewardBalance by remember { mutableStateOf(RewardManager.getRewardBalance(context)) }
    var referralCode by remember { mutableStateOf(ReferralManager.getReferralCode(context)) }
    var referralCount by remember { mutableStateOf(ReferralManager.getReferralCount(context)) }
    var inputReferralCode by remember { mutableStateOf("") }
    var hasAppliedReferral by remember { mutableStateOf(ReferralManager.hasAppliedReferral(context)) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rewards Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reward Balance",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$rewardBalance Points",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Watch Ad Button
                    if (AdManager.canShowRewardedAd(context)) {
                        Button(
                            onClick = {
                                // Load and show rewarded ad
                                AdManager.loadRewardedAd(
                                    context,
                                    onAdLoaded = {
                                        // Ad loaded, now show it
                                        if (context is android.app.Activity) {
                                            AdManager.showRewardedAd(context) { rewardAmount ->
                                                RewardManager.addReward(
                                                    context,
                                                    RewardManager.REWARD_WATCH_AD,
                                                    "Watched rewarded ad"
                                                )
                                                rewardBalance = RewardManager.getRewardBalance(context)
                                                showMessage = "Earned ${RewardManager.REWARD_WATCH_AD} points!"
                                            }
                                        }
                                    },
                                    onAdFailedToLoad = {
                                        showMessage = "Failed to load ad. Try again later."
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Watch Ad (+${RewardManager.REWARD_WATCH_AD} points)")
                        }
                    } else {
                        Text(
                            text = "Come back later for more ads!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Referral Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Referral Program",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Your Referral Code",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = referralCode,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Share this code with friends! Both of you get $${ReferralManager.REFERRAL_REWARD} points.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Successful Referrals: $referralCount",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Apply Referral Code Section
                    if (!hasAppliedReferral) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Have a referral code?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = inputReferralCode,
                            onValueChange = { inputReferralCode = it.uppercase() },
                            label = { Text("Enter Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                ReferralManager.applyReferralCode(context, inputReferralCode) { success ->
                                    if (success) {
                                        hasAppliedReferral = true
                                        rewardBalance = RewardManager.getRewardBalance(context)
                                        showMessage = "Referral code applied! Earned ${ReferralManager.REFERRAL_REWARD} points!"
                                    } else {
                                        showMessage = "Invalid referral code or already used."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = inputReferralCode.length == 6
                        ) {
                            Text("Apply Code (+${ReferralManager.REFERRAL_REWARD} points)")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You've already applied a referral code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Redeem Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Redeem Rewards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coming soon! Use your points to unlock premium features.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Show message snackbar
    showMessage?.let { message ->
        LaunchedEffect(message) {
            // In a real app, show a Snackbar here
            kotlinx.coroutines.delay(3000)
            showMessage = null
        }
    }
}
