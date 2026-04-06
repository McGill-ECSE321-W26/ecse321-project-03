/**
 * backend URL for frontend to call directly
 * we leave it unset so requests are same-origin and vite proxies them (as set in vite.config.js).
 * for production, set VITE_API_BASE_URL in .env file
 */
export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

/**
 * read error message from response body
 * @param {unknown} data - parsed JSON body from a failed response
 * @returns {string}
 */
export function readErrorMessage(data) {
  if (!data) return 'Request failed'
  if (typeof data === 'string') return data
  if (Array.isArray(data.errors) && data.errors.length) return data.errors[0]
  return 'Request failed'
}
