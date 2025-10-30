"use client";

import React, { useEffect, useMemo, useState } from "react";
import { getCoinBalance, addCoins, isPremiumActive, getPremiumDaysRemaining, redeemOneDay, redeemTwoDays, getNextTarget } from "../../lib/rewards";
import BannerAd from "../../components/BannerAd";

export default function RewardsPage() {
  const [coins, setCoins] = useState(0);
  const [premiumDays, setPremiumDays] = useState(0);
  const [referralCode, setReferralCode] = useState("");

  useEffect(() => {
    setCoins(getCoinBalance());
    setPremiumDays(getPremiumDaysRemaining());
    let code = localStorage.getItem("nt_referral_code");
    if (!code) {
      code = Math.random().toString(36).substring(2, 8).toUpperCase();
      localStorage.setItem("nt_referral_code", code);
    }
    setReferralCode(code);
  }, []);

  const [targetCoins, targetDays] = useMemo(() => getNextTarget(), [coins]);
  const progress = Math.min(1, coins / targetCoins);

  const watchAd = async () => {
    // Placeholder "watch ad" simulation (+1 coin)
    addCoins(1);
    setCoins(getCoinBalance());
    alert(`You earned +1 coin`);
  };

  const applyReferral = async (code: string) => {
    if (!code || code.length !== 6) {
      alert("Invalid referral code");
      return;
    }
    if (code === referralCode) {
      alert("Cannot use your own code");
      return;
    }
    // local-only demo: grant +2 coins
    addCoins(2);
    setCoins(getCoinBalance());
    alert(`Referral applied (+2 coins)`);
  };

  return (
    <div className="container">
      <h1>Rewards & Referrals</h1>

      <div className="card mt-16">
        <h2>Ad Coins</h2>
        <p style={{ marginTop: 8, fontSize: 28, fontWeight: 700 }}>{coins}</p>
        <div className="mt-8" style={{ background: "#1e1e25", borderRadius: 8, overflow: "hidden" }}>
          <div style={{ width: `${progress * 100}%`, height: 8, background: "var(--accent)" }} />
        </div>
        <p className="mt-8">Next: {targetCoins} coins → {targetDays} day(s) Premium</p>
        <button className="button mt-8" onClick={watchAd}>Watch Ad (+1)</button>
      </div>

      <div className="card mt-16">
        <h2>Premium</h2>
        <p className="mt-8">
          {isPremiumActive() ? `Active • ${premiumDays} day(s) remaining` : "Inactive"}
        </p>
        <div className="row mt-8">
          <button className="button" onClick={() => { if (redeemOneDay()) { setPremiumDays(getPremiumDaysRemaining()); setCoins(getCoinBalance()); } else alert("Not enough coins (need 7)"); }}>Redeem 1 Day (7)</button>
          <button className="button" onClick={() => { if (redeemTwoDays()) { setPremiumDays(getPremiumDaysRemaining()); setCoins(getCoinBalance()); } else alert("Not enough coins (need 12)"); }}>Redeem 2 Days (12)</button>
        </div>
      </div>

      <div className="card mt-16">
        <h2>Referral</h2>
        <p className="mt-8">Your Code:</p>
        <p style={{ fontSize: 24, fontWeight: 700, color: "var(--accent)" }}>{referralCode}</p>
        <div className="row mt-8">
          <button
            className="button"
            onClick={() => navigator.clipboard.writeText(referralCode)}
          >
            Copy
          </button>
          <button
            className="button"
            onClick={() => {
              const text = `Try NovaTune Web! Use my code ${referralCode}`;
              navigator.share ? navigator.share({ text }) : alert(text);
            }}
          >
            Share
          </button>
        </div>
        <p className="mt-8">Apply a referral code (demo):</p>
        <div className="row mt-8">
          <input id="ref-input" className="input" placeholder="ABC123" maxLength={6} />
          <button className="button" onClick={() => {
            const input = (document.getElementById("ref-input") as HTMLInputElement);
            applyReferral(input.value.toUpperCase());
            input.value = "";
          }}>Apply</button>
        </div>
      </div>

      <BannerAd className="mt-16" />
    </div>
  );
}