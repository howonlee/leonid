# leonid

Full-stack Clojure web app: Clojure (Ring/Jetty/Reitit) backend + ClojureScript (shadow-cljs/Reagent) frontend.

## Layout

```
src/clj/leonid/server.clj   backend: HTTP server, routes, /api/ping
src/cljs/leonid/core.cljs   frontend: Reagent UI
resources/public/index.html SPA entry point
deps.edn                    Clojure deps + aliases
shadow-cljs.edn             ClojureScript build config
```

## Setup

```sh
npm install          # react + shadow-cljs
```

## Develop

Two processes:

```sh
npx shadow-cljs watch app    # compile cljs + hot reload (dev server on :8081)
clojure -M:server            # backend API + serves index.html on :3000
```

Open http://localhost:3000. The "Ping backend" button POSTs to `/api/ping`
and shows the incremented counter returned by the Clojure backend.

(During heavy frontend work you can also use shadow's own dev server at
http://localhost:8081 for the fastest hot-reload loop.)

## Production build

```sh
npm run release              # optimized JS into resources/public/js
clojure -M:server
```
