import React from "react";

export default function BannerAd({ className }: { className?: string }) {
  // Placeholder banner ad. Replace with Google Ad Manager / AdSense script tags.
  return (
    <div className={`card ${className || ""}`} style={{ minHeight: 60, display: "flex", alignItems: "center", justifyContent: "center" }}>
      <span style={{ color: "var(--muted)" }}>Banner Ad (placeholder)</span>
    </div>
  );
}