import { API_BASE, readErrorMessage } from './base.js'

/**
 * Retrieves all items in the cart for a specific account.
 *
 * @param {number} accountId the id of the account
 * @returns {Promise<Object>} the cart items data
 * @throws {Error} if the request fails or the server returns an error
 * @author Yani Zhang
 */
export async function getCartItems(accountId) {
    const res = await fetch(`${API_BASE}/carts/${accountId}/items`)
    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}

/**
 * Removes a specific item type from the account's cart.
 *
 * @param {number} accountId the id of the account
 * @param {number} itemTypeId the id of the item type to remove
 * @returns {Promise<void>}
 * @throws {Error} if the request fails or the server returns an error
 * @author Yani Zhang
 */
export async function removeCartItem(accountId, itemTypeId) {
    const res = await fetch(`${API_BASE}/carts/${accountId}/items/${itemTypeId}`, {
        method: 'DELETE',
    })
    if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        throw new Error(readErrorMessage(data))
    }
}

/**
 * Updates the quantity of a specific item type in the cart.
 *
 * @param {number} accountId the id of the account
 * @param {number} itemTypeId the id of the item type
 * @param {number} quantity the new quantity to set
 * @returns {Promise<Object>} the updated cart item data
 * @throws {Error} if the request fails or the server returns an error
 * @author Yani Zhang
 */
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

/**
 * Retrieves the total cost of all items in the cart for a specific account.
 *
 * @param {number} accountId the id of the account
 * @returns {Promise<Object>} the cart total data
 * @throws {Error} if the request fails or the server returns an error
 * @author Yani Zhang
 */
export async function getCartTotal(accountId) {
    const res = await fetch(`${API_BASE}/carts/${accountId}/total`)
    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}