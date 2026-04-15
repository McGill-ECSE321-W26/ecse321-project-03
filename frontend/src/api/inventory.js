const base = 'http://localhost:8080' // In case API_BASE isn't found
// POST /api/inventory/restock/{itemTypeId}
export async function restockItem(itemTypeId, quantity, accountId) {
    
    const res = await fetch(`${base}/inventory/restock/${itemTypeId}?quantityToAdd=${quantity}&accountId=${accountId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ quantity, accountId }),
    })

    let data
    try {
        data = await res.json()
    } catch {
        data = {}
    }

    if (!res.ok) {
        const msg = data?.message || `Request failed with status ${res.status}`
        throw new Error(msg)
    }

    return data
}


//
