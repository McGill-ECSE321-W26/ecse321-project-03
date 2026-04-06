<template>
  <div class="desktop">
    <div class="window">

      <div class="title-bar"></div>

      <div class="window-body">

        <div class="left">
          <div class="image">
            <img :src="product.imageUrl" :alt="product.name" />
          </div>
          <p class="stock">Stock left: {{ selectedVariant ? selectedVariant.stock : 'Select a color' }}</p>
        </div>

        <div class="right">

          <div class="row">
            <span class="lbl">Name:</span>
            <span class="val">{{ product.name }}</span>
          </div>

          <div class="row">
            <span class="lbl">Price:</span>
            <span class="val">${{ product.price.toFixed(2) }}</span>
          </div>

          <div class="row">
            <span class="lbl">Quantity:</span>
            <div class="qty-ctrl">
              <button @click="qty > 0 && qty--">-</button>
              <span>{{ qty }}</span>
              <button @click="selectedVariant && qty < selectedVariant.stock && qty++">+</button>
            </div>
          </div>

          <div class="colors-block">
            <span class="lbl">Colors:</span>
            <div class="color-grid">
              <button
                v-for="variant in product.variants"
                :key="variant.id"
                class="color-btn"
                :class="{ active: selectedVariant && selectedVariant.id === variant.id }"
                @click="handleSelectVariant(variant)"
              >{{ variant.color }}</button>
            </div>
          </div>

        </div>
      </div>

      <div class="button">
        <button class="cart-btn" @click="handleAddToCart">
          {{ added ? '✓ Added to Cart!' : 'Add to Cart' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script>
import { getProduct, addToCart } from '../api/item.js'

export default {
  name: 'SpecificItemView',

  data() {
    return {
      qty: 0,
      selectedVariant: null, // Replaced selectedColors with selectedVariant
      added: false,
      loading: false,
      product: {
        id: 1,
        name: 'Flowy Blouse',
        price: 29.99,
        imageUrl: 'https://images.unsplash.com/photo-1584030373081-f37b7bb4fa8e?q=80&w=782&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        // Transformed global stock/colors into an array of variant objects
        variants: [
          { id: 101, color: 'White', stock: 12 },
          { id: 102, color: 'Brown', stock: 5 },
          { id: 103, color: 'Black', stock: 23 },
          { id: 104, color: 'Grey', stock: 0 } // Example of out of stock
        ]
      },
    }
  },

  methods: {
    handleSelectVariant(variant) {
      this.selectedVariant = variant;
      
      // Auto-adjust quantity if it exceeds the new variant's stock, 
      // or set it to 1 if it was 0 and stock is available
      if (this.qty > variant.stock) {
        this.qty = variant.stock;
      } else if (this.qty === 0 && variant.stock > 0) {
        this.qty = 1;
      }
    },

    async handleAddToCart() {
      if (!this.selectedVariant || this.qty === 0) {
        alert("Please select a color and valid quantity!");
        return;
      }
      
      if (this.qty > this.selectedVariant.stock) {
        alert("Not enough stock for this variant!");
        return;
      }

      await addToCart(1, this.selectedVariant.id, this.qty)
      this.added = true
      setTimeout(() => this.added = false, 2000)
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inconsolata:wght@200..900&family=Jersey+10&display=swap');

* { box-sizing: border-box; margin: 0; padding: 0; }

.desktop {
  min-height: 100vh;
  background: #c0c0c0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  font-family: 'Inconsolata', monospace;
}

.window {
  width: 620px;
  background: #fff;
  border: 3px solid #1209AB;
  box-shadow: 12px 12px 0 #3D3C3C;
}

.title-bar {
  background: #b39ddb;
  height: 44px;
  border-bottom: 3px solid #1209AB;
}

.window-body {
  display: flex;
  gap: 32px;
  padding: 28px 28px 16px;
}

.left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.image {
  width: 200px;
  height: 220px;
  background: #f2f2f2;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stock {
  font-size: 13px;
  color: #333;
}

.right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.lbl {
  font-size: 16px;
  font-weight: 700;
  color: #222;
  white-space: nowrap;
}

.val {
  background: #F7F7F7;
  border-radius: 20px;
  padding: 5px 18px;
  font-size: 14px;
  color: #333;
}

.qty-ctrl {
  display: flex;
  align-items: center;
  background: #F7F7F7;
  border-radius: 20px;
  overflow: hidden;
  padding: 0 4px;
  gap: 2px;
}
.qty-ctrl button {
  width: 28px;
  height: 30px;
  background: none;
  border: none;
  font-size: 16px;
  font-family: 'Inconsolata', monospace;
  cursor: pointer;
  color: #333;
}
.qty-ctrl button:hover { background: #ddd; border-radius: 50%; }
.qty-ctrl span {
  width: 32px;
  text-align: center;
  font-size: 14px;
}

.colors-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px 4px;
}
 
.color-btn {
  padding: 5px 14px;
  border: 1px solid #ccc;
  border-radius: 20px;
  background: #fff;
  font-family: 'Inconsolata', monospace;
  font-size: 13px;
  color: #222;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.color-btn:hover { border-color: #1209AB; }
.color-btn.active {
  background: #1209AB;
  color: #fff;
  border-color: #1209AB;
}

.button {
  padding: 16px 28px 24px;
  display: flex;
  justify-content: center;
}

.cart-btn {
  background: #f8bbd0;
  border: 2px solid #888;
  border-radius: 24px;
  padding: 10px 48px;
  font-family: 'Inconsolata', monospace;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
  box-shadow: 2px 2px 0 #aaa;
}
.cart-btn:hover { background: #f48fb1; transform: translate(-1px,-1px); box-shadow: 3px 3px 0 #aaa; }
.cart-btn:active { transform: translate(1px,1px); box-shadow: 1px 1px 0 #aaa; }
</style>