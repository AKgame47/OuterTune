const KEY_COINS = "nt_coins";
const KEY_PREMIUM_START = "nt_premium_start";
const KEY_PREMIUM_EXPIRE = "nt_premium_expire";

export function getCoinBalance(): number {
  if (typeof window === "undefined") return 0;
  return parseInt(localStorage.getItem(KEY_COINS) || "0", 10);
}

export function addCoins(amount: number): void {
  if (typeof window === "undefined") return;
  const current = getCoinBalance();
  localStorage.setItem(KEY_COINS, String(current + amount));
}

function spendCoins(amount: number): boolean {
  const current = getCoinBalance();
  if (current < amount) return false;
  localStorage.setItem(KEY_COINS, String(current - amount));
  return true;
}

function grantPremium(days: number): void {
  const start = Date.now();
  const expire = start + days * 24 * 60 * 60 * 1000;
  localStorage.setItem(KEY_PREMIUM_START, String(start));
  localStorage.setItem(KEY_PREMIUM_EXPIRE, String(expire));
}

export function redeemOneDay(): boolean {
  if (!spendCoins(7)) return false;
  grantPremium(1);
  return true;
}

export function redeemTwoDays(): boolean {
  if (!spendCoins(12)) return false;
  grantPremium(2);
  return true;
}

export function isPremiumActive(): boolean {
  if (typeof window === "undefined") return false;
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

export function getPremiumDaysRemaining(): number {
  if (!isPremiumActive()) return 0;
  const expire = parseInt(localStorage.getItem(KEY_PREMIUM_EXPIRE) || "0", 10);
  const millisRemaining = expire - Date.now();
  return Math.floor(millisRemaining / (24 * 60 * 60 * 1000));
}

export function getNextTarget(): [number, number] {
  const balance = getCoinBalance();
  if (balance < 7) return [7, 1];
  if (balance < 12) return [12, 2];
  return [7, 1];
}