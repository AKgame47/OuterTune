import "./globals.css";
import React from "react";

export const metadata = {
  title: "NovaTune Web",
  description: "NovaTune advanced web app (PWA-ready scaffold)"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
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