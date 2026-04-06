import { API_BASE, readErrorMessage } from './base.js'

export async function getCartItems(accountId) {
    const res = await fetch(`${API_BASE}/carts/${accountId}/items`)
    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}

export async function removeCartItem(accountId, itemTypeId) {
    const res = await fetch(`${API_BASE}/carts/${accountId}/items/${itemTypeId}`, {
        method: 'DELETE',
    })
    if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        throw new Error(readErrorMessage(data))
    }
}

export async function updateCartItemQuantity(accountId, itemTypeId, quantity) {
    const res = await fetch(`${API_BASE}/carts/${accountId}/items/${itemTypeId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ quantity }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}

export async function getCartTotal(accountId) {
    const res = await fetch(`${API_BASE}/carts/${accountId}/total`)
    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}