/**
 * backend URL for frontend to call directly
 * we leave it unset so requests are same-origin and vite proxies them (as set in vite.config.js).
 * for production, set VITE_API_BASE_URL in .env file
 */
export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

function readErrorMessage(data) {
  if (!data) return 'Request failed'
  if (typeof data === 'string') return data
  if (Array.isArray(data.errors) && data.errors.length) return data.errors[0]
  return 'Request failed'
}

/**
 * login with email and password
 * @param {string} email - email of the account
 * @param {string} password - password of the account
 * @returns {Promise<Object>} - account information if successful, error message if not
 * 
 * @author Zirun Wang
 */
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

/**
 * register a new account
 * @param {Object} payload - account information
 * @returns {Promise<Object>} - account information if successful, error message if not
 * 
 * @author Zirun Wang
 */
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
