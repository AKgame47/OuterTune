import "./globals.css";
import React from "react";
import Script from "next/script";

export const metadata = {
  title: "NovaTune Web",
  description: "NovaTune advanced web app (PWA-ready scaffold)"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const adsenseClient = process.env.NEXT_PUBLIC_ADSENSE_CLIENT;

  return (
    <html lang="en">
      <head>
        {adsenseClient && (
          <Script
            id="adsense-js"
            async
            src={`https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${adsenseClient}`}
            crossOrigin="anonymous"
            strategy="afterInteractive"
          />
        )}
      </head>
      <body>
        <header className="site-header">
          <nav className="nav">
            <a href="/" className="brand">NovaTune</a>
            <div className="spacer" />
            <a href="/search">Search</a>
            <a href="/rewards">Rewards</a>
          </nav>
        </header>
        <main className="container">
          {children}
        </main>
        <footer className="site-footer">
          <small>Modified from OuterTune (GPL-3.0). © NovaTune</small>
        </footer>
      </body>
    </html>
  );
}