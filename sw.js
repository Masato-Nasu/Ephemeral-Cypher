// sw.js (v1.1.0) — force-update friendly
const VERSION = '1.1.0';
const CACHE = `ephemeralcypher-${VERSION}`;
const ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE);
    await cache.addAll(ASSETS);
    await self.skipWaiting();
  })());
});

self.addEventListener('activate', (event) => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.map(k => (k !== CACHE) ? caches.delete(k) : Promise.resolve()));
    await self.clients.claim();
  })());
});

async function cacheFirst(req){
  const cache = await caches.open(CACHE);
  const hit = await cache.match(req, { ignoreSearch: true });
  if (hit) return hit;
  const res = await fetch(req);
  if (res && res.ok) cache.put(req, res.clone());
  return res;
}

async function networkFirst(req){
  const cache = await caches.open(CACHE);
  try{
    const res = await fetch(req, { cache: 'no-store' });
    if (res && res.ok) cache.put(req, res.clone());
    return res;
  }catch(e){
    const hit = await cache.match(req, { ignoreSearch: true });
    if (hit) return hit;
    throw e;
  }
}

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  const accept = req.headers.get('accept') || '';
  const isHTML = req.mode === 'navigate' || accept.includes('text/html');

  if (isHTML) {
    event.respondWith(networkFirst(req));
    return;
  }

  event.respondWith(cacheFirst(req));
});
