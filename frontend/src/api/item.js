import { API_BASE, readErrorMessage } from './base.js'

const base = API_BASE

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

export async function createCart(accountId) {
  const res = await fetch(`${base}/carts/${accountId}`, {
    method: 'POST',
  })
  const text = await res.text()
  if (!res.ok) throw new Error(`Failed to create cart: ${readErrorMessage(text)}`)
  return text ? JSON.parse(text) : null
}

export async function addToCart(accountId, variantId, quantity) {
  const tryAdd = async () => fetch(`${base}/carts/${accountId}/items`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ itemType: { id: variantId }, quantity })
  });

  let res = await tryAdd();

  if (!res.ok) {
    const text = await res.text();
    if (text.includes("doesn't have a cart")) {
      await createCart(accountId);
      res = await tryAdd();
    } else {
      throw new Error(`Failed to add to cart: ${readErrorMessage(text)}`);
    }
  }

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

export async function updateItem(accountId, itemId, dto) {
  const formData = new FormData();

  const itemUpdateDto = {
    name: dto.name,
    price: dto.price,
    tag: dto.tag,
    // Backend requires variants — pass existing ones through unchanged
    variants: dto.variants.map(v => ({
      size: v.size,
      color: v.color,
      initialStock: v.stockLevel ?? v.stock ?? 0
    }))
  };

  formData.append('item', new Blob([JSON.stringify(itemUpdateDto)], { type: 'application/json' }));

  if (dto.imageFile) {
    formData.append('files', dto.imageFile);
  }

  const url = `${base}/items/${itemId}?accountId=${accountId}`;
  const res = await fetch(url, {
    method: 'PUT',
    body: formData
  });

  const text = await res.text();
  if (!res.ok) throw new Error(`Failed to update item: ${readErrorMessage(text)}`);
  return text ? JSON.parse(text) : null;
}

export async function deleteItem(accountId, itemId) {
  const url = `${base}/items/${itemId}?accountId=${accountId}`;
  const res = await fetch(url, {
    method: 'DELETE',
  });

  const text = await res.text();
  if (!res.ok) throw new Error(`Failed to delete item: ${readErrorMessage(text)}`);
  return true;
}