// sw.js (v1.0.2) — simpler and Android-friendly
const CACHE = 'ephemeralcypher-v1.0.2';
const ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS)));
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.map(k => k !== CACHE && caches.delete(k)))));
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Navigation requests → offline fallback to index.html
  if (request.mode === 'navigate') {
    event.respondWith((async () => {
      try {
        const fresh = await fetch(request);
        const cache = await caches.open(CACHE);
        cache.put('./index.html', fresh.clone());
        return fresh;
      } catch (e) {
        const cache = await caches.open(CACHE);
        return (await cache.match('./index.html')) || Response.error();
      }
    })());
    return;
  }

  // App assets → stale-while-revalidate
  if (ASSETS.includes(url.pathname.endsWith('/') ? './' : '.' + url.pathname.substring(url.pathname.lastIndexOf('/')))) {
    event.respondWith((async () => {
      const cache = await caches.open(CACHE);
      const cached = await cache.match(request);
      const network = fetch(request).then(res => {
        if (res && res.ok) cache.put(request, res.clone());
        return res;
      }).catch(() => null);
      return cached || network || Response.error();
    })());
    return;
  }

  // Other requests (e.g., allorigins) → network-first with fallback cache
  event.respondWith((async () => {
    try {
      const res = await fetch(request);
      return res;
    } catch (e) {
      const cache = await caches.open(CACHE);
      const cached = await cache.match(request);
      return cached || Response.error();
    }
  })());
});
