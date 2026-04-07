import { API_BASE, readErrorMessage } from './base.js'

const base = API_BASE || 'http://localhost:8080'

// GET all items
export async function getAllItems(accountId) {
  const url = `${base}/items?accountId=${accountId}`
  const res = await fetch(url)
  const text = await res.text()
  if (!res.ok) throw new Error(`Failed to fetch items: ${readErrorMessage(text)}`)
  return text ? JSON.parse(text) : []
}

export async function getProduct(id, accountId) {
  const res = await fetch(`${base}/items/${id}?accountId=${accountId}`)
  const text = await res.text()
  if (!res.ok) throw new Error(`Failed to fetch product: ${readErrorMessage(text)}`)
  return text ? JSON.parse(text) : null
}

// POST add to cart (Updated to match your Vue component!)
export async function addToCart(accountId, variantId, quantity) {
  const res = await fetch(`${base}/cart`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ accountId, variantId, quantity })
  })
  const text = await res.text()
  if (!res.ok) throw new Error(`Failed to add to cart: ${readErrorMessage(text)}`)
  return text ? JSON.parse(text).catch(() => true) : true
}

// GET cart
export async function getCart() {
  const res = await fetch(`${base}/cart`)
  const text = await res.text()
  if (!res.ok) throw new Error(`Failed to fetch cart: ${readErrorMessage(text)}`)
  return text ? JSON.parse(text) : []
}

// DELETE from cart
export async function removeFromCart(productId) {
  const res = await fetch(`${base}/cart/${productId}`, { method: 'DELETE' })
  const text = await res.text()
  if (!res.ok) throw new Error(`Failed to remove from cart: ${readErrorMessage(text)}`)
  return text ? JSON.parse(text).catch(() => true) : true
}