// Helpers
function getApiKey() {
  return localStorage.getItem("yt_api_key") || "";
}
function saveApiKey() {
  const k = document.getElementById("yt-api-key").value.trim();
  if (!k) return alert("Enter an API key");
  localStorage.setItem("yt_api_key", k);
  alert("Saved API key");
}
window.saveApiKey = saveApiKey;

// Play YouTube URL (Search page)
function playYouTubeUrl() {
  const url = document.getElementById("yt-url").value.trim();
  const id = extractYouTubeId(url);
  if (!id) return alert("Invalid YouTube URL");
  const iframe = document.getElementById("yt-player");
  iframe.src = `https://www.youtube.com/embed/${id}?autoplay=1`;
  // Fetch recommendations via API if key exists
  const key = getApiKey();
  if (key) fetchRecommendations(id, key);
}
window.playYouTubeUrl = playYouTubeUrl;

// Play YouTube URL (Home page)
function playYouTubeUrlHome() {
  const url = document.getElementById("yt-url-home").value.trim();
  const id = extractYouTubeId(url);
  if (!id) return alert("Invalid YouTube URL");
  const iframe = document.getElementById("yt-player-home");
  iframe.src = `https://www.youtube.com/embed/${id}?autoplay=1`;
}
window.playYouTubeUrlHome = playYouTubeUrlHome;

function extractYouTubeId(url) {
  try {
    const u = new URL(url);
    if (u.hostname.includes("youtu.be")) return u.pathname.slice(1);
    if (u.searchParams.get("v")) return u.searchParams.get("v");
    return null;
  } catch {
    return null;
  }
}

// Search: YouTube if API key, else iTunes fallback
async function runSearch() {
  const q = document.getElementById("query").value || "music";
  const results = document.getElementById("results");
  results.innerHTML = "";
  const key = getApiKey();

  if (key) {
    const url = `https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=25&q=${encodeURIComponent(q)}&key=${key}`;
    const res = await fetch(url);
    if (!res.ok) {
      return alert("YouTube search failed. Check API key quota/permissions.");
    }
    const data = await res.json();
    data.items.forEach((it, idx) => {
      const vid = it.id.videoId;
      const card = document.createElement("div");
      card.className = "card";
      card.style.marginBottom = "8px";
      card.innerHTML = `
        <div class="row space-between">
          <div>
            <strong>${escapeHtml(it.snippet.title)}</strong>
            <p class="mt-8 muted">${escapeHtml(it.snippet.channelTitle)}</p>
          </div>
          <button class="button" onclick="playVideo('${vid}')">Play</button>
        </div>
      `;
      results.appendChild(card);
      if (idx !== 0 && idx % 12 === 6) {
        const ad = document.createElement("div");
        ad.className = "card";
        ad.style.marginTop = "8px";
        ad.innerHTML = "<strong>Sponsored</strong><p class='mt-8 muted'>Native Ad (placeholder)</p>";
        results.appendChild(ad);
      }
    });
  } else {
    // iTunes fallback (previews only)
    const url = `https://itunes.apple.com/search?term=${encodeURIComponent(q)}&media=music&limit=25`;
    const res = await fetch(url);
    if (!res.ok) return alert("iTunes search failed");
    const data = await res.json();
    data.results.forEach((it, idx) => {
      const card = document.createElement("div");
      card.className = "card";
      card.style.marginBottom = "8px";
      card.innerHTML = `
        <div class="row space-between">
          <div>
            <strong>${escapeHtml(it.trackName || it.collectionName || "Track")}</strong>
            <p class="mt-8 muted">${escapeHtml(it.artistName || "")}</p>
          </div>
          <button class="button" onclick="playPreview('${it.previewUrl}')">Preview</button>
        </div>
      `;
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
}
window.runSearch = runSearch;

function playVideo(id) {
  const iframe = document.getElementById("yt-player");
  iframe.src = `https://www.youtube.com/embed/${id}?autoplay=1`;
  const key = getApiKey();
  if (key) fetchRecommendations(id, key);
}
window.playVideo = playVideo;

function playPreview(url) {
  const audio = document.getElementById("audioPlayer");
  audio.src = url;
  audio.play();
}
window.playPreview = playPreview;

async function fetchRecommendations(videoId, key) {
  const rec = document.getElementById("recommendations");
  rec.innerHTML = "";
  // Use "search" with relatedToVideoId
  const url = `https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=10&relatedToVideoId=${encodeURIComponent(videoId)}&key=${key}`;
  const res = await fetch(url);
  if (!res.ok) return;
  const data = await res.json();
  data.items.forEach(it => {
    const vid = it.id.videoId;
    const card = document.createElement("div");
    card.className = "card";
    card.style.marginBottom = "8px";
    card.innerHTML = `
      <div class="row space-between">
        <div>
          <strong>${escapeHtml(it.snippet.title)}</strong>
          <p class="mt-8 muted">${escapeHtml(it.snippet.channelTitle)}</p>
        </div>
        <button class="button" onclick="playVideo('${vid}')">Play</button>
      </div>
    `;
    rec.appendChild(card);
  });
}

function escapeHtml(s) {
  return s.replace(/[&<>"']/g, (m) => ({ "&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;","'":"&#39;" }[m]));
}