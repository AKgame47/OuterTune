import React, { useEffect, useRef } from "react";

/**
 * AdSense banner slot. If NEXT_PUBLIC_ADSENSE_CLIENT and NEXT_PUBLIC_ADSENSE_SLOT are provided,
 * renders a real AdSense banner. Otherwise, shows a placeholder card.
 */
export default function BannerAd({ className }: { className?: string }) {
  const client = process.env.NEXT_PUBLIC_ADSENSE_CLIENT;
  const slot = process.env.NEXT_PUBLIC_ADSENSE_SLOT;
  const ref = useRef<HTMLDivElement>(null);

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
      <div className={`card ${className || ""}`} style={{ minHeight: 60, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <span style={{ color: "var(--muted)" }}>Banner Ad (placeholder)</span>
      </div>
    );
  }

  return (
    <div className={className} ref={ref}>
      <ins
        className="adsbygoogle"
        style={{ display: "block" }}
        data-ad-client={client}
        data-ad-slot={slot}
        data-ad-format="auto"
        data-full-width-responsive="true"
      />
    </div>
  );
}