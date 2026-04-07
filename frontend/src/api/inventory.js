import { API_BASE, readErrorMessage } from './base.js'

// POST /api/inventory/restock/{itemTypeId}
export async function restockItem(itemTypeId, quantity, accountId) {
    const res = await fetch(`${API_BASE}/inventory/restock/${itemTypeId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ itemTypeId, quantity, accountId }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(readErrorMessage(data))
    return data
}


//
