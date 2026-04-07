import { API_BASE, readErrorMessage } from './base.js'

/**
 * Method to retrieve all items in the backend
 * @author Maria Klinski
 */
export async function getAllItems(accountId) {
  const base = API_BASE || 'http://localhost:8080'

  const url = `${base}/items?accountId=${accountId}`
  console.log("Fetching URL:", url)

  const res = await fetch(url)
  const text = await res.text()

  console.log("Status:", res.status)
  console.log("Content-Type:", res.headers.get("content-type"))
  console.log("Raw response:", text)

  if (!res.ok) {
    const errorMsg = readErrorMessage(text)
    throw new Error(`Failed to get all items: ${res.status} ${errorMsg}`)
  }

  if (!text) {
    return []
  }

  return JSON.parse(text)
}

/**
 * Method to add a specific variant to the user's cart
 * @author [Your Name]
 */
export async function addToCart(accountId, variantId, quantity) {
  const base = API_BASE || 'http://localhost:8080'

  const url = `${base}/cart`
  console.log("Fetching URL:", url)

  const payload = {
    accountId: accountId,
    variantId: variantId,
    quantity: quantity
  }
  console.log("Payload:", payload)

  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })

  const text = await res.text()

  console.log("Status:", res.status)
  console.log("Content-Type:", res.headers.get("content-type"))
  console.log("Raw response:", text)

  if (!res.ok) {
    const errorMsg = readErrorMessage(text)
    throw new Error(`Failed to add to cart: ${res.status} ${errorMsg}`)
  }

  if (!text) {
    return true // Return true if the backend successfully added it but didn't send JSON back
  }

  return JSON.parse(text)
}