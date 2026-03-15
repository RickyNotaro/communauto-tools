# communauto-tools

Browse available Communauto (car-sharing) vehicles in Montreal via the ReserveAuto API.

- **Véhicules** — List all available vehicles, sort by distance from your location, filter by radius
- **Radar** — OpenStreetMap view with vehicle markers and your position

## Stack

Vue 3.5 · TypeScript 5 · Vite 6 · Leaflet · Axios · Bootstrap 5

## Setup

```
npm install
```

### Development

```
npm run dev
```

### Build for production

```
npm run build
```

### Preview production build

```
npm run preview
```

### Lint

```
npm run lint
```

## CORS Proxy (Cloudflare Worker)

The production build uses a Cloudflare Worker to proxy requests to the ReserveAuto API with CORS headers.

### Setup

```bash
cd workers/cors-proxy
npm install
```

### Local development

```bash
npm run dev
```

### Deploy

```bash
npm run deploy
```

After deploying, set `VITE_CORS_PROXY_URL` in your `.env.production` to your worker URL:

```
VITE_CORS_PROXY_URL=https://communauto-cors-proxy.<your-subdomain>.workers.dev
```

The worker allows configuring `ALLOWED_ORIGIN` (comma-separated) and `TARGET_HOST` via `wrangler.toml` or the Cloudflare dashboard.
