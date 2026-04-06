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
 * Redeems loyalty points for an existing order.
 *
 * @param {number} orderId
 * @param {number} accountId
 * @param {boolean} usePoints
 * @returns {Promise<string>}
 * @author Yani Zhang
 */
export async function redeemLoyaltyPoints(orderId, accountId, usePoints) {
    const params = new URLSearchParams({
        accountId: String(accountId),
        usePoints: String(usePoints),
    })

    const res = await fetch(`${API_BASE}/orders/redeem-loyalty/${orderId}?${params}`, {
        method: 'POST',
    })

    const text = await res.text().catch(() => '')
    if (!res.ok) {
        try {
            const parsed = JSON.parse(text)
            throw new Error(readErrorMessage(parsed))
        } catch {
            throw new Error(text || 'Failed to redeem loyalty points')
        }
    }

    return text
}