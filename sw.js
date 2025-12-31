/* EphemeralCypher SW v1.1.2 */
const CACHE = "ephemeralcypher-112";
const ASSETS = [
  "./",
  "./index.html",
  "./manifest.json",
  "./icon-192.png",
  "./icon-512.png"
];

self.addEventListener("install", (event) => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE).then((c) => c.addAll(ASSETS.map(u => u + "?v=112")).catch(()=>c.addAll(ASSETS).catch(()=>null)))
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.map(k => (k !== CACHE) ? caches.delete(k) : Promise.resolve()));
    await self.clients.claim();
  })());
});

self.addEventListener("fetch", (event) => {
  const req = event.request;
  const url = new URL(req.url);

  // Only handle same-origin
  if (url.origin !== self.location.origin) return;

  // Navigation: network-first
  if (req.mode === "navigate") {
    event.respondWith((async () => {
      try {
        const fresh = await fetch(req, {cache:"no-store"});
        const cache = await caches.open(CACHE);
        cache.put("./", fresh.clone()).catch(()=>null);
        return fresh;
      } catch {
        const cache = await caches.open(CACHE);
        return (await cache.match("./")) || (await cache.match("./index.html")) || Response.error();
      }
    })());
    return;
  }

  // Static: stale-while-revalidate
  event.respondWith((async () => {
    const cache = await caches.open(CACHE);
    const cached = await cache.match(req, {ignoreSearch:true});
    const fetchPromise = fetch(req).then((res) => {
      if (res && res.ok) cache.put(req, res.clone()).catch(()=>null);
      return res;
    }).catch(()=>null);

    return cached || (await fetchPromise) || Response.error();
  })());
});
