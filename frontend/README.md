# Vue 3 + Vite

## 1. Development guide

To talk to the Spring Boot API from the UI:

1. **Add or extend `src/api/client.js`.** Define async functions that call backend (for example `/accounts/...`, `/items/...`). 

2. **Use those functions from the relevant `.vue` file.** Import them in `<script setup>`, call them from event handlers or `onMounted` with **`await`**, and wrap logic in **`try` / `catch`** so failures surface in the UI.

## 2. Important files

| File | Purpose |
|------|---------|
| `index.html` | Entry; HTML shell; loads `src/main.js` and mounts the app on `#app`. |
| `src/main.js` | App entry: global styles, Vue app, Pinia, router, `mount("#app")`. |
| `src/App.vue` | Root component; renders the active route via `<RouterView />`. |
| `vite.config.js` | Vite and Vue plugin; dev **proxy** forwards `/accounts`, `/items`, `/images`, `/carts`, `/orders`, `/inventory` to the backend. |
| `src/api/client.js` | **Backend access:** `fetch` helpers, shared `API_BASE`, error handling. |
| `src/session/storage.js` | Login session in `sessionStorage` (`saveSession`, `clearSession`, `getSession`, `isLoggedIn`). |
| `src/router/index.js` | Route definitions (`/login`, `/app`) and **navigation guards** (auth vs guest) using `session/storage`. |
| `src/style.css` | Global CSS shared across views. |

