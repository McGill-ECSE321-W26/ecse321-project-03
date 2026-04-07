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
  return text ? JSON.parse(text) : true
}


export async function createItem(accountId, itemData, files = null) {
  const formData = new FormData();
  
  
  const itemCreationDto = {
    name: itemData.name,
    price: itemData.price,
    tag: itemData.tag,
    variants: itemData.variants.map(variant => ({
      size: variant.size,
      color: variant.color,
      initialStock: variant.initialStock
    }))
  };
  
  
  formData.append('item', new Blob([JSON.stringify(itemCreationDto)], { type: 'application/json' }));
  
 
  if (files && files.length) {
    files.forEach(file => {
      formData.append('files', file);
    });
  }
  
  const url = `${base}/items?accountId=${accountId}`;
  const res = await fetch(url, {
    method: 'POST',
    body: formData
    
  });
  
  const text = await res.text();
  if (!res.ok) throw new Error(`Failed to create item: ${readErrorMessage(text)}`);
  return text ? JSON.parse(text) : null;
}
