import {API_BASE, readErrorMessage} from './base.js'

/**
 * Method to retrieve all the orders in the backend
 * @author Sonia Ly
 */
export async function getAllOrders() {
    const res = await fetch(`${API_BASE}/order`)
    if (!res.ok) throw new Error(readErrorMessage(data))
    return res.json()
}