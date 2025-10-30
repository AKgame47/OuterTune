/*
 * Copyright (C) 2025 NovaTune
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.novatune.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novatune.app.ads.AdManager
import com.novatune.app.referral.ReferralManager
import com.novatune.app.rewards.RewardManager

/**
 * Rewards & Referrals screen:
 * - Shows coin balance
 * - Watch ad to earn coins
 * - Premium days remaining, and coin redemption
 * - Referral code with copy/share, apply code
 */
@Composable
fun RewardsFragment(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbar = rememberSnackbarHostState()

    var coinBalance by remember { mutableIntStateOf(RewardManager.getCoinBalance(context)) }
    var premiumDays by remember { mutableIntStateOf(RewardManager.getPremiumDaysRemaining(context)) }
    var referralCode by remember { mutableStateOf(ReferralManager.getReferralCode(context)) }
    var referralCount by remember { mutableIntStateOf(ReferralManager.getReferralCount(context)) }
    var inputReferralCode by remember { mutableStateOf("") }
    var hasAppliedReferral by remember { mutableStateOf(ReferralManager.hasAppliedReferral(context)) }
    val (nextTargetCoins, nextTargetDays) = RewardManager.getNextTarget(context)
    val progress = (coinBalance.toFloat() / nextTargetCoins.toFloat()).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coin Balance + Watch Ad
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ad Coins", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$coinBalance",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Next: $nextTargetCoins coins → $nextTargetDays day(s) Premium",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))

                        if (AdManager.canShowRewardedAd(context)) {
                            Button(
                                onClick = {
                                    AdManager.loadRewardedAd(
                                        context,
                                        onAdLoaded = {
                                            if (context is android.app.Activity) {
                                                AdManager.showRewardedAd(context) {
                                                    RewardManager.addCoins(
                                                        context,
                                                        RewardManager.COINS_WATCH_AD,
                                                        "Watched rewarded ad"
                                                    )
                                                    coinBalance = RewardManager.getCoinBalance(context)
                                                    snackbar.showSnackbar(
                                                        "You earned +${RewardManager.COINS_WATCH_AD} coin",
                                                        withDismissAction = true,
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        },
                                        onAdFailedToLoad = {
                                            snackbar.showSnackbar(
                                                "Failed to load ad. Try again later.",
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Watch Ad (+${RewardManager.COINS_WATCH_AD})")
                            }
                        } else {
                            Text(
                                text = "Come back later for more ads",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Premium status + Redeem
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Premium Access", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (RewardManager.isPremiumActive(context))
                                "Active • ${premiumDays} day(s) remaining"
                            else "Inactive",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (RewardManager.redeemOneDayPremium(context)) {
                                    premiumDays = RewardManager.getPremiumDaysRemaining(context)
                                    coinBalance = RewardManager.getCoinBalance(context)
                                    snackbar.showSnackbar("Premium activated for 1 day", withDismissAction = true)
                                } else {
                                    snackbar.showSnackbar("Not enough coins (need 7)", withDismissAction = true)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Redeem 1 Day (7 coins)")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (RewardManager.redeemTwoDaysPremium(context)) {
                                    premiumDays = RewardManager.getPremiumDaysRemaining(context)
                                    coinBalance = RewardManager.getCoinBalance(context)
                                    snackbar.showSnackbar("Premium activated for 2 days", withDismissAction = true)
                                } else {
                                    snackbar.showSnackbar("Not enough coins (need 12)", withDismissAction = true)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Redeem 2 Days (12 coins)")
                        }
                    }
                }
            }

            // Referral
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Referral Program", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Your Code")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            referralCode,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Referral Code", referralCode))
                                // show snackbar
                                LaunchedEffect(Unit) {
                                    snackbar.showSnackbar("Copied", withDismissAction = true, duration = SnackbarDuration.Short)
                                }
                            }) { Text("Copy") }
                            OutlinedButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Try NovaTune! Use my code $referralCode")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share referral code"))
                            }) { Text("Share") }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Successful Referrals: $referralCount", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Each valid referral grants +${RewardManager.COINS_REFERRAL_SUCCESS} coin(s) to you and the new user.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!hasAppliedReferral) {
                            Spacer(Modifier.height(16.dp))
                            Divider()
                            Spacer(Modifier.height(16.dp))
                            Text("Have a referral code?", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputReferralCode,
                                onValueChange = { inputReferralCode = it.uppercase() },
                                label = { Text("Enter Code") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    ReferralManager.applyReferralCode(context, inputReferralCode) { success ->
                                        if (success) {
                                            hasAppliedReferral = true
                                            coinBalance = RewardManager.getCoinBalance(context)
                                            LaunchedEffect(Unit) {
                                                snackbar.showSnackbar(
                                                    "Referral applied (+${RewardManager.COINS_REFERRAL_SUCCESS} coins)",
                                                    withDismissAction = true
                                                )
                                            }
                                        } else {
                                            LaunchedEffect(Unit) {
                                                snackbar.showSnackbar(
                                                    "Invalid code or already used",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                    }
                                },
                                enabled = inputReferralCode.length == 6,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply Code")
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "You've already applied a referral code.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
