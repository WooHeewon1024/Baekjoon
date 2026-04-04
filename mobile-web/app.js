const cityInput = document.getElementById("cityInput");
const searchBtn = document.getElementById("searchBtn");
const gpsBtn = document.getElementById("gpsBtn");
const installBtn = document.getElementById("installBtn");
const statusText = document.getElementById("statusText");
const locationText = document.getElementById("locationText");
const sunText = document.getElementById("sunText");
const timeText = document.getElementById("timeText");

const canvas = document.getElementById("compassCanvas");
const ctx = canvas.getContext("2d");

let latitude = 37.5666;
let longitude = 126.9782;
let cityName = "서울";
let updateTimer = null;
let deferredInstallPrompt = null;

let currentAzimuth = 180;
let currentElevation = 45;

function normalize360(value) {
  let v = value % 360;
  if (v < 0) v += 360;
  return v;
}

function cardinalName(azimuth) {
  const dirs = [
    "북", "북북동", "북동", "동북동",
    "동", "동남동", "남동", "남남동",
    "남", "남남서", "남서", "서남서",
    "서", "서북서", "북서", "북북서",
  ];
  const idx = Math.floor((normalize360(azimuth) + 11.25) / 22.5) % 16;
  return dirs[idx];
}

function formatCoords(lat, lon) {
  const ns = lat >= 0 ? "N" : "S";
  const ew = lon >= 0 ? "E" : "W";
  return `(${Math.abs(lat).toFixed(4)}°${ns}, ${Math.abs(lon).toFixed(4)}°${ew})`;
}

function calculateSun(latitudeDeg, longitudeDeg, date) {
  const utcMillis = date.getTime();

  const jd = utcMillis / 86400000 + 2440587.5;
  const T = (jd - 2451545.0) / 36525.0;

  let L0 = (280.46646 + T * (36000.76983 + T * 0.0003032)) % 360;
  if (L0 < 0) L0 += 360;

  const M = 357.52911 + T * (35999.05029 - 0.0001537 * T);
  const Mrad = (M * Math.PI) / 180;

  const C =
    (1.914602 - T * (0.004817 + 0.000014 * T)) * Math.sin(Mrad) +
    (0.019993 - 0.000101 * T) * Math.sin(2 * Mrad) +
    0.000289 * Math.sin(3 * Mrad);

  const sunTrueLon = L0 + C;
  const omega = 125.04 - 1934.136 * T;
  const lambda = sunTrueLon - 0.00569 - 0.00478 * Math.sin((omega * Math.PI) / 180);

  const eps0 =
    23 +
    (26 +
      (21.448 -
        T * (46.815 + T * (0.00059 - T * 0.001813))) /
        60) /
      60;
  const epsilon = eps0 + 0.00256 * Math.cos((omega * Math.PI) / 180);

  const lambdaRad = (lambda * Math.PI) / 180;
  const epsilonRad = (epsilon * Math.PI) / 180;

  const sinDec = Math.sin(epsilonRad) * Math.sin(lambdaRad);
  const declination = (Math.asin(sinDec) * 180) / Math.PI;

  const L0rad = (L0 * Math.PI) / 180;
  const e = 0.016708634 - T * (0.000042037 + 0.0000001267 * T);
  let yy = Math.tan(epsilonRad / 2);
  yy *= yy;

  const eotDeg =
    yy * Math.sin(2 * L0rad) -
    2 * e * Math.sin(Mrad) +
    4 * e * yy * Math.sin(Mrad) * Math.cos(2 * L0rad) -
    0.5 * yy * yy * Math.sin(4 * L0rad) -
    1.25 * e * e * Math.sin(2 * Mrad);
  const eotMinutes = (eotDeg * 180 * 4) / Math.PI;

  let utcMinutes = (utcMillis % 86400000) / 60000;
  if (utcMinutes < 0) utcMinutes += 1440;

  const trueSolarTime = normalize360((utcMinutes + eotMinutes + 4 * longitudeDeg) / 4) * 4;
  const hourAngle = trueSolarTime / 4 - 180;

  const latRad = (latitudeDeg * Math.PI) / 180;
  const decRad = (declination * Math.PI) / 180;
  const haRad = (hourAngle * Math.PI) / 180;

  let cosZenith =
    Math.sin(latRad) * Math.sin(decRad) +
    Math.cos(latRad) * Math.cos(decRad) * Math.cos(haRad);
  cosZenith = Math.max(-1, Math.min(1, cosZenith));

  const zenithDeg = (Math.acos(cosZenith) * 180) / Math.PI;
  let elevation = 90 - zenithDeg;

  if (elevation > -0.575) {
    let refArcSec = 0;
    if (elevation > 85) {
      refArcSec = 0;
    } else if (elevation > 5) {
      const t = Math.tan((elevation * Math.PI) / 180);
      refArcSec = 58.1 / t - 0.07 / (t * t * t) + 0.000086 / Math.pow(t, 5);
    } else {
      refArcSec = 1735 + elevation * (-518.2 + elevation * (103.4 + elevation * (-12.79 + elevation * 0.711)));
    }
    elevation += refArcSec / 3600;
  }

  const sinZenith = Math.sin((Math.max(0.001, zenithDeg) * Math.PI) / 180);
  let azimuth = 0;
  if (sinZenith >= 1e-8) {
    let cosAz =
      (Math.sin(latRad) * cosZenith - Math.sin(decRad)) /
      (Math.cos(latRad) * sinZenith);
    cosAz = Math.max(-1, Math.min(1, cosAz));
    const azRaw = (Math.acos(cosAz) * 180) / Math.PI;
    azimuth = hourAngle > 0 ? normalize360(azRaw + 180) : normalize360(540 - azRaw);
  }

  return { azimuth, elevation };
}

function drawCompass(azimuth, elevation) {
  const w = canvas.width;
  const h = canvas.height;
  const cx = w / 2;
  const cy = h / 2;
  const R = Math.min(w, h) / 2 - 24;

  ctx.clearRect(0, 0, w, h);

  const night = elevation < 0;
  const grad = ctx.createRadialGradient(cx, cy, R * 0.15, cx, cy, R);
  if (night) {
    grad.addColorStop(0, "#172346");
    grad.addColorStop(1, "#0b122f");
  } else {
    grad.addColorStop(0, "#8fd2ff");
    grad.addColorStop(1, "#2f8ad6");
  }

  ctx.fillStyle = grad;
  ctx.beginPath();
  ctx.arc(cx, cy, R, 0, Math.PI * 2);
  ctx.fill();

  ctx.strokeStyle = "rgba(255,255,255,0.72)";
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.arc(cx, cy, R, 0, Math.PI * 2);
  ctx.stroke();

  ctx.strokeStyle = "rgba(255,255,255,0.3)";
  ctx.setLineDash([4, 4]);
  [30, 60].forEach((elev) => {
    const rr = R * ((90 - elev) / 90);
    ctx.beginPath();
    ctx.arc(cx, cy, rr, 0, Math.PI * 2);
    ctx.stroke();
  });
  ctx.setLineDash([]);

  const dirs = ["N", "NE", "E", "SE", "S", "SW", "W", "NW"];
  dirs.forEach((label, i) => {
    const deg = i * 45;
    const a = ((deg - 90) * Math.PI) / 180;
    const tx = cx + (R + 16) * Math.cos(a);
    const ty = cy + (R + 16) * Math.sin(a);

    ctx.fillStyle = label === "N" ? "#ff6b6b" : "#eff6ff";
    ctx.font = label.length === 1 ? "700 15px Space Grotesk" : "600 11px Space Grotesk";
    const width = ctx.measureText(label).width;
    ctx.fillText(label, tx - width / 2, ty + 5);
  });

  if (night) {
    ctx.fillStyle = "rgba(201, 212, 255, 0.95)";
    ctx.beginPath();
    const moonAngle = ((azimuth - 90) * Math.PI) / 180;
    const mx = cx + (R - 20) * Math.cos(moonAngle);
    const my = cy + (R - 20) * Math.sin(moonAngle);
    ctx.arc(mx, my, 9, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = "#0d1537";
    ctx.beginPath();
    ctx.arc(mx + 4, my - 1, 8, 0, Math.PI * 2);
    ctx.fill();
    return;
  }

  const clampedElev = Math.max(0, Math.min(90, elevation));
  const dist = R * (1 - clampedElev / 90);
  const sunAngle = ((azimuth - 90) * Math.PI) / 180;
  const sx = cx + dist * Math.cos(sunAngle);
  const sy = cy + dist * Math.sin(sunAngle);

  ctx.strokeStyle = "rgba(255, 225, 124, 0.6)";
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.moveTo(cx, cy);
  ctx.lineTo(sx, sy);
  ctx.stroke();

  const glow = ctx.createRadialGradient(sx, sy, 2, sx, sy, 20);
  glow.addColorStop(0, "rgba(255, 216, 107, 0.95)");
  glow.addColorStop(1, "rgba(255, 216, 107, 0)");
  ctx.fillStyle = glow;
  ctx.beginPath();
  ctx.arc(sx, sy, 20, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = "#ffd35a";
  ctx.beginPath();
  ctx.arc(sx, sy, 9, 0, Math.PI * 2);
  ctx.fill();

  ctx.strokeStyle = "#f5a200";
  ctx.lineWidth = 1.5;
  ctx.beginPath();
  ctx.arc(sx, sy, 9, 0, Math.PI * 2);
  ctx.stroke();
}

function updateSunPosition() {
  const now = new Date();
  const sun = calculateSun(latitude, longitude, now);
  currentAzimuth = sun.azimuth;
  currentElevation = sun.elevation;

  drawCompass(currentAzimuth, currentElevation);

  locationText.textContent = `위치: ${cityName} ${formatCoords(latitude, longitude)}`;
  sunText.textContent = `방위각: ${currentAzimuth.toFixed(1)}° | 고도: ${currentElevation.toFixed(1)}° | 방향: ${cardinalName(currentAzimuth)}`;

  const localApproxOffset = Math.round(longitude / 15);
  const utcMs = now.getTime() + now.getTimezoneOffset() * 60000;
  const localApprox = new Date(utcMs + localApproxOffset * 3600000);
  timeText.textContent = `현지시각(추정): ${localApprox.toLocaleTimeString("ko-KR")} | 업데이트: ${now.toLocaleTimeString("ko-KR")}`;

  if (currentElevation < 0) {
    statusText.textContent = "현재 야간입니다. 해가 지평선 아래에 있습니다.";
  } else {
    statusText.textContent = "실시간으로 태양 방향을 계산 중입니다.";
  }
}

async function searchCity(city) {
  const query = encodeURIComponent(city);
  const url = `https://nominatim.openstreetmap.org/search?q=${query}&format=json&limit=1`;
  const response = await fetch(url, {
    headers: {
      "Accept": "application/json",
    },
  });
  if (!response.ok) {
    throw new Error(`도시 검색 실패 (HTTP ${response.status})`);
  }

  const data = await response.json();
  if (!Array.isArray(data) || data.length === 0) {
    return null;
  }

  return {
    latitude: Number(data[0].lat),
    longitude: Number(data[0].lon),
    displayName: data[0].display_name || city,
  };
}

async function onSearch() {
  const city = cityInput.value.trim();
  if (!city) return;

  searchBtn.disabled = true;
  statusText.textContent = "도시 검색 중...";

  try {
    const result = await searchCity(city);
    if (!result) {
      statusText.textContent = `도시를 찾을 수 없습니다: ${city}`;
      return;
    }

    latitude = result.latitude;
    longitude = result.longitude;
    cityName = city;
    statusText.textContent = `도시 적용 완료: ${result.displayName}`;
    updateSunPosition();
  } catch (err) {
    statusText.textContent = `검색 오류: ${err.message}`;
  } finally {
    searchBtn.disabled = false;
  }
}

function onGps() {
  if (!navigator.geolocation) {
    statusText.textContent = "이 브라우저는 위치 기능을 지원하지 않습니다.";
    return;
  }

  gpsBtn.disabled = true;
  statusText.textContent = "현재 위치를 가져오는 중...";

  navigator.geolocation.getCurrentPosition(
    (position) => {
      latitude = position.coords.latitude;
      longitude = position.coords.longitude;
      cityName = "현재 위치";
      updateSunPosition();
      statusText.textContent = "현재 위치 적용 완료";
      gpsBtn.disabled = false;
    },
    (error) => {
      statusText.textContent = `위치 오류: ${error.message}`;
      gpsBtn.disabled = false;
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 30000,
    }
  );
}

function setupPwaInstall() {
  window.addEventListener("beforeinstallprompt", (event) => {
    event.preventDefault();
    deferredInstallPrompt = event;
    installBtn.classList.remove("hidden");
  });

  installBtn.addEventListener("click", async () => {
    if (!deferredInstallPrompt) return;
    deferredInstallPrompt.prompt();
    await deferredInstallPrompt.userChoice;
    deferredInstallPrompt = null;
    installBtn.classList.add("hidden");
  });

  if ("serviceWorker" in navigator) {
    navigator.serviceWorker.register("./service-worker.js").catch(() => {
      // Registration failure should not block app usage.
    });
  }
}

function boot() {
  searchBtn.addEventListener("click", onSearch);
  cityInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") onSearch();
  });
  gpsBtn.addEventListener("click", onGps);

  setupPwaInstall();

  updateSunPosition();
  if (updateTimer) clearInterval(updateTimer);
  updateTimer = setInterval(updateSunPosition, 10000);
}

boot();
