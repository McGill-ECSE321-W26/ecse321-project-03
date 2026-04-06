export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

// GET /api/products/{id}
export async function getProduct(id) {
  const res = await fetch(`${API_BASE}/products/${id}`)
  if (!res.ok) throw new Error('Failed to fetch product')
  return res.json()
}

// GET /api/products
export async function getAllProducts() {
  const res = await fetch(`${API_BASE}/products`)
  if (!res.ok) throw new Error('Failed to fetch products')
  return res.json()
}

// POST /api/cart
export async function addToCart(productId, quantity, colors) {
  const res = await fetch(`${API_BASE}/cart`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId, quantity, colors })
  })
  if (!res.ok) throw new Error('Failed to add to cart')
  return res.json()
}

// GET /api/cart
export async function getCart() {
  const res = await fetch(`${BASE_URL}/cart`)
  if (!res.ok) throw new Error('Failed to fetch cart')
  return res.json()
}

// DELETE /api/cart/:productId
export async function removeFromCart(productId) {
  const res = await fetch(`${BASE_URL}/cart/${productId}`, {
    method: 'DELETE'
  })
  if (!res.ok) throw new Error('Failed to remove from cart')
  return res.json()
}