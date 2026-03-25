# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- **Dev server:** `npm run dev` (Vite)
- **Build:** `npm run build` (runs vue-tsc type-check then vite build)
- **Preview production build:** `npm run preview`
- **Lint:** `npm run lint`

## Architecture

Vue 3.5 + TypeScript 5 + Vite 6 app for browsing available Communauto (car-sharing) vehicles via the ReserveAuto API. UI labels are in French. All components use `<script setup>` syntax.

### Data Flow

`http-common.ts` configures an Axios instance pointing at `https://www.reservauto.net/`. `CommunautoDataService` exports functions wrapping API calls. Components fetch data in `onMounted` via the service.

### API Endpoints

Base URL: `https://www.reservauto.net/`

**Legacy — `GetVehicleProposals`** (no auth)
`GET /WCF/LSI/LSIBookingService.svc/GetVehicleProposals`
- Optional params: `CustomerID`, `Latitude`, `Longitude`
- Response: `{ Vehicules: Vehicule[] }`
- Typed as `Vehicule` (Id, Name, ModelName, Immat, EnergyLevel, Position)

**V3 — `GetAvailableVehicles`** (no auth)
`GET /WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles`
- Required params: `BranchID` (1=Quebec), `LanguageID` (1=EN, 2=FR)
- Optional params: `CityID` (59=Montreal)
- Response: `{ Success: boolean, Vehicles: VehicleV3[] }`
- Typed as `VehicleV3` — richer data: CarBrand, CarColor, CarSeatNb, IsElectric, LastUseDate, BookingStatus, CarAccessories, IsVehicleReturnFlex

**Authenticated REST API** (OAuth 2.0, not currently used)
`https://restapifrontoffice.reservauto.net` — Swagger docs for Vehicle, Station, City, Branch, Rental, Customer, etc.

### Key Types

- `Vehicule` — legacy entity (GetVehicleProposals response)
- `VehicleV3` — V3 entity with full vehicle details (GetAvailableVehicles response)
- `VehiclePromotion` — promotion metadata on vehicles
- Types use named exports (`export interface`)

### Routes

- `/` — Home (landing page)
- `/vehicules` — Two-panel list: vehicle list + selected vehicle detail
- `/radar` — Google Maps view with markers for each vehicle (centered on Montreal)

### Component Relationships

- `views/VehiculeList.vue` → `components/VehiculesList.vue` → `components/VehiculeDetail.vue`
- `views/Radar.vue` → `components/Radar.vue` (uses `vue3-google-map`)

### Legacy

`public/radar.html` is a standalone jQuery/Google Maps page (not part of the Vue app). It predates the Vue rewrite and uses JSONP for the same API.
