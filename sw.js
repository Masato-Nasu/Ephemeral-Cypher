// sw.js
const VERSION = 'v1.0.0';
const APP_CACHE = `app-${VERSION}`;
const RUNTIME_CACHE = `rt-${VERSION}`;

// インストール：アプリシェルをキャッシュ
self.addEventListener('install', (event) => {
  event.waitUntil((async () => {
    const cache = await caches.open(APP_CACHE);
    await cache.addAll([
      './',
      './index.html',
      './manifest.json',
      './icon-192.png',
      './icon-512.png'
    ]);
  })());
  self.skipWaiting();
});

// 有効化：古いキャッシュ削除
self.addEventListener('activate', (event) => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.map(k => {
      if (k !== APP_CACHE && k !== RUNTIME_CACHE) return caches.delete(k);
    }));
  })());
  self.clients.claim();
});

const APP_SHELL = new Set([
  location.origin + location.pathname.replace(/\/[^\/]*$/, '/') ,
  location.origin + location.pathname.replace(/\/[^\/]*$/, '/') + 'index.html',
  location.origin + location.pathname.replace(/\/[^\/]*$/, '/') + 'manifest.json',
  location.origin + location.pathname.replace(/\/[^\/]*$/, '/') + 'icon-192.png',
  location.origin + location.pathname.replace(/\/[^\/]*$/, '/') + 'icon-512.png'
]);

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);

  if (APP_SHELL.has(url.href)) {
    event.respondWith((async () => {
      const cache = await caches.open(APP_CACHE);
      const cached = await cache.match(req);
      const fetchPromise = fetch(req).then(res => {
        if (res.ok) cache.put(req, res.clone());
        return res;
      }).catch(() => null);
      return cached || fetchPromise || new Response('Offline', { status: 503 });
    })());
    return;
  }

  event.respondWith((async () => {
    try {
      const res = await fetch(req);
      const cache = await caches.open(RUNTIME_CACHE);
      if (res && res.ok && (url.origin === location.origin || url.hostname.endsWith('allorigins.win'))) {
        cache.put(req, res.clone());
      }
      return res;
    } catch (e) {
      const cache = await caches.open(RUNTIME_CACHE);
      const cached = await cache.match(req);
      if (cached) return cached;
      if (req.destination === 'document') {
        const appCache = await caches.open(APP_CACHE);
        const index = await appCache.match('./index.html');
        if (index) return index;
      }
      return new Response('Offline', { status: 503 });
    }
  })());
});
