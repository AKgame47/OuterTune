import React from "react";
import BannerAd from "../components/BannerAd";
import NativeAdCard from "../components/NativeAdCard";

export default function HomePage() {
  return (
    <div className="container">
      <h1>NovaTune Web</h1>
      <p className="mt-8">Welcome to the NovaTune advanced web app scaffold.</p>

      <div className="card mt-16">
        <h2>Library</h2>
        <p className="mt-8">This is a placeholder view. Implement your web data layer here.</p>
        <NativeAdCard className="mt-16" />
      </div>

      <BannerAd className="mt-16" />
    </div>
  );
}