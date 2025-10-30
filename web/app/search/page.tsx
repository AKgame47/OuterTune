"use client";

import React, { useMemo, useState } from "react";
import NativeAdCard from "../../components/NativeAdCard";

export default function SearchPage() {
  const [query, setQuery] = useState("");
  const [items, setItems] = useState<string[]>([]);

  const onSearch = async () => {
    // Placeholder search: generate dummy items
    const result = Array.from({ length: 25 }).map((_, i) => `${query || "Item"} ${i + 1}`);
    setItems(result);
  };

  const content = useMemo(() => {
    const blocks: React.ReactNode[] = [];
    items.forEach((it, idx) => {
      blocks.push(
        <div key={it} className="card" style={{ marginBottom: 8 }}>
          {it}
        </div>
      );
      if (idx !== 0 && idx % 12 === 6) {
        blocks.push(<NativeAdCard key={`ad-${idx}`} className="mt-8" />);
      }
    });
    return blocks;
  }, [items]);

  return (
    <div className="container">
      <h1>Search</h1>
      <div className="row mt-16">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="input"
          placeholder="Search..."
        />
        <button className="button" onClick={onSearch}>Search</button>
      </div>

      <div className="mt-16">{content}</div>
    </div>
  );
}