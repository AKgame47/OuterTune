import React, { useEffect } from "react";

/**
 * AdSense in-article native ad. Requires NEXT_PUBLIC_ADSENSE_CLIENT and NEXT_PUBLIC_ADSENSE_INARTICLE_SLOT.
 * Falls back to placeholder when not configured.
 */
export default function NativeAdCard({ className }: { className?: string }) {
  const client = process.env.NEXT_PUBLIC_ADSENSE_CLIENT;
  const slot = process.env.NEXT_PUBLIC_ADSENSE_INARTICLE_SLOT;

  useEffect(() => {
    if (client && slot && typeof window !== "undefined") {
      try {
        // @ts-ignore
        (window.adsbygoogle = window.adsbygoogle || []).push({});
      } catch {
        // ignore
      }
    }
  }, [client, slot]);

  if (!client || !slot) {
    return (
      <div className={`card ${className || ""}`} style={{ transition: "opacity 300ms ease-in" }}>
        <strong>Sponsored</strong>
        <p style={{ marginTop: 8, color: "var(--muted)" }}>
          Native Ad (placeholder). Configure NEXT_PUBLIC_ADSENSE_CLIENT and NEXT_PUBLIC_ADSENSE_INARTICLE_SLOT.
        </p>
      </div>
    );
  }

  return (
    <div className={className}>
      <ins className="adsbygoogle"
        style={{ display: "block", textAlign: "center" }}
        data-ad-layout="in-article"
        data-ad-format="fluid"
        data-ad-client={client}
        data-ad-slot={slot}
      />
    </div>
  );
}