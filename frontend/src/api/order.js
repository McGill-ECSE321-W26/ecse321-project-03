import {API_BASE, readErrorMessage} from './base.js'

/**
 * Method to retrieve all the orders in the backend
 * @author Sonia Ly
 */
export async function getAllOrders(userId) {
    const base = API_BASE

    const url = `${base}/order/order?userID=${userId}`
    console.log("Fetching URL:", url)

    const res = await fetch(url)
    const text = await res.text()

    console.log("Status:", res.status)
    console.log("Content-Type:", res.headers.get("content-type"))
    console.log("Raw response:", text)

    if (!res.ok) {
        throw new Error(`Failed to get all orders: ${res.status} ${text}`)
    }

    if (!text) {
        return []
    }

    return JSON.parse(text)
}