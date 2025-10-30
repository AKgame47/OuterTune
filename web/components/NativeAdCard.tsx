import React from "react";

export default function NativeAdCard({ className }: { className?: string }) {
  // Simple native ad card placeholder with rounded corners and elevation.
  return (
    <div className={`card ${className || ""}`} style={{ transition: "opacity 300ms ease-in" }}>
      <strong>Sponsored</strong>
      <p style={{ marginTop: 8, color: "var(--muted)" }}>
        Native Ad (placeholder). Integrate Google Ad Manager native ad here.
      </p>
    </div>
  );
}