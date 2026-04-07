import { API_BASE, readErrorMessage } from './base.js'

/**
 * Creates an order and performs checkout in a single operation.
 *
 * @param {Object} payload
 * @param {number} payload.customerId
 * @param {string} payload.delivDate ISO-8601 date string
 * @returns {Promise<Object>}
 * @author Yani Zhang
 */
export async function placeOrder(payload) {
    const res = await fetch(`${API_BASE}/orders/checkout`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}

export async function getLoyaltyPoints(accountId) {
    const res = await fetch(`${API_BASE}/accounts/${accountId}/loyalty-points`)
    const data = await res.json().catch(() => 0)
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}

/**
 * * Calculates the final total price of an order after loyalty discount.
 *
 * @param {number} orderId
 * @param {number} accountId
 * @returns {Promise<number>}
 * @author Yani Zhang
 */
export async function calculateFinalPriceAfterDiscount(orderId, accountId) {
    const params = new URLSearchParams({
        accountId: String(accountId),
    })

    const res = await fetch(`${API_BASE}/orders/${orderId}/final-price?${params}`)
    const data = await res.json().catch(() => 0)
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}