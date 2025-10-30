// Simple search demo
function runSearch() {
  const q = document.getElementById("query").value || "Item";
  const results = document.getElementById("results");
  results.innerHTML = "";

  const items = Array.from({ length: 25 }).map((_, i) => `${q} ${i + 1}`);
  items.forEach((it, idx) => {
    const card = document.createElement("div");
    card.className = "card";
    card.style.marginBottom = "8px";
    card.textContent = it;
    results.appendChild(card);

    if (idx !== 0 && idx % 12 === 6) {
      const ad = document.createElement("div");
      ad.className = "card";
      ad.style.marginTop = "8px";
      ad.innerHTML = "<strong>Sponsored</strong><p class='mt-8 muted'>Native Ad (placeholder)</p>";
      results.appendChild(ad);
    }
  });
}