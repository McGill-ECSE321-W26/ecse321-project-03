# Vue 3 + Vite

## 1. Development guide

To talk to the Spring Boot API from the UI:

1. **Add or extend `src/api/` modules.** Put shared URL and error helpers in `src/api/base.js` (`API_BASE`, `readErrorMessage`). Put domain-specific `fetch` functions in files such as `src/api/account.js` (for example `/accounts/...`); add more files as needed (items, inventory, etc.).

2. **Use those functions from the relevant `.vue` file.** Import them in `<script setup>`, call them from event handlers or `onMounted` with **`await`**, and wrap logic in **`try` / `catch`** so failures surface in the UI.

## 2. Important files

| File | Purpose |
|------|---------|
| `index.html` | Entry; HTML shell; loads `src/main.js` and mounts the app on `#app`. |
| `src/main.js` | App entry: global styles, Vue app, Pinia, router, `mount("#app")`. |
| `src/App.vue` | Root component; renders the active route via `<RouterView />`. |
| `vite.config.js` | Vite and Vue plugin; dev **proxy** forwards `/accounts`, `/items`, `/images`, `/carts`, `/orders`, `/inventory` to the backend. |
| `src/api/base.js` | Shared `API_BASE` and `readErrorMessage` for all API modules. |
| `src/session/storage.js` | Login session in `sessionStorage` (`saveSession`, `clearSession`, `getSession`, `isLoggedIn`). |
| `src/router/index.js` | Route definitions (`/login`, `/app`) and **navigation guards** (auth vs guest) using `session/storage`. |
| `src/style.css` | Global CSS shared across views. |

