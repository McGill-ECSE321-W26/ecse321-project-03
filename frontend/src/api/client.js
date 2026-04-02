/**
 * Base URL for the Spring Boot API.
 * In development, leave unset so requests are same-origin and Vite proxies them (see vite.config.js).
 * For production builds served separately, set VITE_API_BASE_URL (e.g. http://localhost:8080).
 */
export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

function readErrorMessage(data) {
  if (!data) return 'Request failed'
  if (typeof data === 'string') return data
  if (Array.isArray(data.errors) && data.errors.length) return data.errors[0]
  return 'Request failed'
}

export async function loginWithEmailPassword(email, password) {
  const res = await fetch(`${API_BASE}/accounts/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ email, password }),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(readErrorMessage(data))
  return data
}

export async function registerAccount(payload) {
  const res = await fetch(`${API_BASE}/accounts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(readErrorMessage(data))
  return data
}
