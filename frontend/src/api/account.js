import { API_BASE, readErrorMessage} from './base.js'

/**
 * Login with email and password.
 * @param {string} email
 * @param {string} password
 * @returns {Promise<Object>}
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
 * Register a new account.
 * @param {Object} payload
 * @returns {Promise<Object>}
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

export async function getAccountById(accountId) {
  const res = await fetch(`${API_BASE}/accounts/${accountId}`)
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(readErrorMessage(data))
  return data
}

export async function updateAddress(accountId, address) {
  const res = await fetch(`${API_BASE}/accounts/${accountId}/address`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ address }),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(readErrorMessage(data))
  return data
}

/**
 * Update account. Body matches AccountCreationDto (username, email, password, address; accountType optional).
 * @param {number} id
 * @param {Object} payload
 * @returns {Promise<Object>}
 */
export async function updateAccount(id, payload) {
  const res = await fetch(`${API_BASE}/accounts/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(readErrorMessage(data))
  return data
}

/**
 * Sends a request to remove an account by its ID.
 * @param id
 * @returns {Promise<void>}
 */
export async function deleteAccount(id) {
  const res = await fetch(`${API_BASE}/accounts/${id}`, {
    method: 'DELETE',
  })

  const text = await res.text()
  let data = {}

  try {
    data = text ? JSON.parse(text) : {}
  } catch {
    data = { message: text }
  }

  console.log("DELETE /accounts status:", res.status)
  console.log("DELETE /accounts response:", data)

  if (!res.ok) {
    throw new Error(
        readErrorMessage(data) || data.message || `Request failed with status ${res.status}`
    )
  }
}