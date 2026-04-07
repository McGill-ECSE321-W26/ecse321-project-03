<template>
  <div class="desktop">
    <div class="window">

      <div class="title-bar">
        <button class="back-btn" @click="$router.back()">← Back</button>
        <span class="title-name">{{ product.name }}</span>
      </div>

      <div class="window-body">

        <div class="left">
          <div class="image">
            <img :src="product.imageUrl" :alt="product.name" />
          </div>
          <p class="stock">Stock left: {{ selectedVariant ? selectedVariant.stock : 'Select Color & Size' }}</p>
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
                v-for="color in uniqueColors"
                :key="color"
                class="color-btn"
                :class="{ active: selectedColor === color }"
                @click="handleSelectColor(color)"
              >{{ color }}</button>
            </div>
          </div>

          <div class="colors-block" v-if="selectedColor">
            <span class="lbl">Sizes:</span>
            <div class="color-grid">
              <button
                v-for="size in availableSizesForColor"
                :key="size"
                class="color-btn"
                :class="{ active: selectedSize === size }"
                @click="handleSelectSize(size)"
              >{{ size }}</button>
            </div>
          </div>

        </div>
      </div>

      <div class="button">
        <button class="cart-btn" @click="handleAddToCart">
          {{ added ? '✓ Added to Cart!' : 'Add to Cart' }}
        </button>
      </div>
     <!-- Manager-only Restock Button -->
      <div v-if="isManager" class="button">
        <button class="cart-btn" @click="showRestockForm = true">
          Restock
        </button>
      </div>

    </div>

    <!-- Restock Modal using RestockItemView.vue -->
    <RestockItemView
      :visible="showRestockForm"
      :product="product"
      @close="showRestockForm = false"
      @restock="handleRestockSubmit"
    />
  </div>
</template>

<script>
import { getSession } from '../session/storage'
import { addToCart } from '../api/item.js'
import RestockItemView from './RestockItemView.vue'  // <-- Import your actual RestockItemView
import { restockItem } from '../api/inventory.js';

export default {
  name: 'SpecificItemView',
  components: { RestockItemView },

  data() {
    const session = getSession()
    const accountId = session?.accountId

    return {
      isManager: true,
      //session?.accountType === 'MANAGER',
      showRestockForm: false,
      errorMessage: '',
      qty: 0,
      selectedColor: null, 
      selectedSize: null,  // Added size tracking
      added: false,
      loading: false,
      product: {
        id: 1,
        name: 'Flowy Blouse',
        price: 29.99,
        imageUrl: 'https://images.unsplash.com/photo-1584030373081-f37b7bb4fa8e?w=400&q=80',
        // Updated mock data to reflect variants with both colors and sizes
        variants: [
          { id: 101, color: 'White', size: 'S', stock: 12 },
          { id: 102, color: 'White', size: 'M', stock: 5 },
          { id: 103, color: 'Black', size: 'S', stock: 23 },
          { id: 104, color: 'Black', size: 'L', stock: 0 } 
        ]
      },
    }
  },

  // ── uncomment when backend is ready ──────────────────
  // async mounted() {
  //   const id = this.$route.params.id
  //   this.product = await getProduct(id, 1)
  //   this.loading = false
  // },
  // ─────────────────────────────────────────────────────

  computed: {
    // Finds all unique colors
    uniqueColors() {
      if (!this.product || !this.product.variants) return [];
      const colors = this.product.variants.map(v => v.color);
      return [...new Set(colors)]; 
    },
    
    // Finds sizes available specifically for the chosen color
    availableSizesForColor() {
      if (!this.product || !this.product.variants || !this.selectedColor) return [];
      const variantsWithColor = this.product.variants.filter(v => v.color === this.selectedColor);
      return variantsWithColor.map(v => v.size);
    },

    // Pinpoints the exact variant ID based on the color + size picked
    selectedVariant() {
      if (!this.product || !this.selectedColor || !this.selectedSize) return null;
      return this.product.variants.find(
        v => v.color === this.selectedColor && v.size === this.selectedSize
      );
    }
  },

  methods: {
    handleSelectColor(color) {
      this.selectedColor = color;
      this.selectedSize = null; // Reset size when color changes
      this.qty = 0;             // Reset quantity to prevent going over stock
    },

    handleSelectSize(size) {
      this.selectedSize = size;
      
      // Auto-adjust quantity so it doesn't exceed the newly selected size's stock
      this.$nextTick(() => {
        if (this.selectedVariant && this.qty > this.selectedVariant.stock) {
          this.qty = this.selectedVariant.stock;
        } else if (this.qty === 0 && this.selectedVariant && this.selectedVariant.stock > 0) {
          this.qty = 1;
        }
      });
    },

    async handleAddToCart() {
      if (!this.selectedColor || !this.selectedSize) {
        alert("Please select both a color and a size!");
        return;
      }
      
      if (this.qty === 0) {
        alert("Please select a valid quantity!");
        return;
      }

      if (this.qty > this.selectedVariant.stock) {
        alert("Not enough stock for this variant!");
        return;
      }

      // Sends accountId (1), the specific variant id, and qty
      await addToCart(1, this.selectedVariant.id, this.qty)
      this.added = true
      setTimeout(() => this.added = false, 2000)
    },

    //commented off until backend is ready
    //async handleRestockSubmit(restockData) {
    //  errorMessage.value = ''
    //  try{
    //    await restockItem(restockData.id, restockData.quantity, restockData.accountId)
    //  } catch (error) {
    //    errorMessage.value = error.message || 'Failed to load cart items.'
    //  }
    //}

    handleRestockSubmit(restockData){
      //palecholder until the backend is connected properly
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
  background: #D8BFF1;
  height: 44px;
  border-bottom: 3px solid #1209AB;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center; /* keeps title centered */
}

.back-btn {
  position: absolute;
  left: 10px;

  background:#FFE5F9;
  border: 0.5px solid #000000;
  border-radius: 20px;
  padding: 6px 14px;

  font-family: 'Inconsolata', monospace;
  font-size: 14px;
  cursor: pointer;

  box-shadow: 2px 2px 0 #aaa;
  transition: opacity 0.15s, transform 0.1s;
}

.back-btn:hover {
  opacity: 0.85;
  transform: translate(-1px,-1px);
  box-shadow: 3px 3px 0 #aaa;
}

.back-btn:active {
  transform: translate(1px,1px);
  box-shadow: 1px 1px 0 #aaa;
}

.title-name {
  font-family: 'Inconsolata', monospace;
  font-weight: 700;
  color: #000000;
  letter-spacing: 0.05em;
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
  background:#f8bbd0;
  border: 0.5px solid #000000;
  border-radius: 28px;

  padding: 12px 56px;

  font-family: 'Inconsolata', monospace;;
  font-size: 18px;

  cursor: pointer;

  box-shadow: 3px 3px 0 #8a7a86;
  transition: all 0.1s ease;
}

.cart-btn:hover {
  transform: translate(-1px, -1px);
  box-shadow: 4px 4px 0 #8a7a86;
}

.cart-btn:active {
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 #8a7a86;
}
</style>