const KEY_COINS = "nt_coins";
const KEY_PREMIUM_START = "nt_premium_start";
const KEY_PREMIUM_EXPIRE = "nt_premium_expire";

function getCoins() {
  return parseInt(localStorage.getItem(KEY_COINS) || "0", 10);
}
function setCoins(v) {
  localStorage.setItem(KEY_COINS, String(v));
}
function addCoins(amount) {
  setCoins(getCoins() + amount);
}
function spendCoins(amount) {
  const c = getCoins();
  if (c < amount) return false;
  setCoins(c - amount);
  return true;
}

function grantPremium(days) {
  const start = Date.now();
  const expire = start + days * 24 * 60 * 60 * 1000;
  localStorage.setItem(KEY_PREMIUM_START, String(start));
  localStorage.setItem(KEY_PREMIUM_EXPIRE, String(expire));
}
function isPremiumActive() {
  const expire = parseInt(localStorage.getItem(KEY_PREMIUM_EXPIRE) || "0", 10);
  const now = Date.now();
  if (expire <= now) {
    if (expire !== 0) {
      localStorage.setItem(KEY_PREMIUM_START, "0");
      localStorage.setItem(KEY_PREMIUM_EXPIRE, "0");
    }
    return false;
  }
  return true;
}
function getPremiumDaysRemaining() {
  if (!isPremiumActive()) return 0;
  const expire = parseInt(localStorage.getItem(KEY_PREMIUM_EXPIRE) || "0", 10);
  const millis = expire - Date.now();
  return Math.floor(millis / (24 * 60 * 60 * 1000));
}
function getNextTarget() {
  const c = getCoins();
  if (c < 7) return [7, 1];
  if (c < 12) return [12, 2];
  return [7, 1];
}

function refreshUI() {
  const coins = getCoins();
  document.getElementById("coins").textContent = coins;
  const [targetCoins, targetDays] = getNextTarget();
  document.getElementById("next-target").textContent = `Next: ${targetCoins} coins → ${targetDays} day(s) Premium`;
  const progress = Math.min(1, coins / targetCoins);
  document.getElementById("bar-progress").style.width = `${progress * 100}%`;

  const status = isPremiumActive() ? `Active • ${getPremiumDaysRemaining()} day(s) remaining` : "Inactive";
  document.getElementById("premium-status").textContent = status;

  let code = localStorage.getItem("nt_referral_code");
  if (!code) {
    code = Math.random().toString(36).substring(2, 8).toUpperCase();
    localStorage.setItem("nt_referral_code", code);
  }
  document.getElementById("ref-code").textContent = code;
}

function watchAd() {
  addCoins(1);
  refreshUI();
  alert("You earned +1 coin");
}

function redeem(days) {
  if (days === 1) {
    if (!spendCoins(7)) return alert("Not enough coins (need 7)");
    grantPremium(1);
  } else {
    if (!spendCoins(12)) return alert("Not enough coins (need 12)");
    grantPremium(2);
  }
  refreshUI();
  alert(`Premium activated for ${days} day(s)`);
}

function copyCode() {
  const code = document.getElementById("ref-code").textContent;
  navigator.clipboard.writeText(code);
  alert("Copied");
}

function shareCode() {
  const code = document.getElementById("ref-code").textContent;
  const text = `Try NovaTune Web! Use my code ${code}`;
  if (navigator.share) navigator.share({ text });
  else alert(text);
}

function applyReferral() {
  const input = document.getElementById("ref-input");
  const code = input.value.toUpperCase();
  const myCode = document.getElementById("ref-code").textContent;
  if (!code || code.length !== 6) return alert("Invalid referral code");
  if (code === myCode) return alert("Cannot use your own code");
  addCoins(2);
  refreshUI();
  alert("Referral applied (+2 coins)");
  input.value = "";
}

window.watchAd = watchAd;
window.redeem = redeem;
window.copyCode = copyCode;
window.shareCode = shareCode;
window.applyReferral = applyReferral;

document.addEventListener("DOMContentLoaded", refreshUI);