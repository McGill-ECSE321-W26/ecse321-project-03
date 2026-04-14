<template>
  <div class="page">

    <Header activeTab="shop" />

    <div class="page-inner">

      <div class="window">

        <div class="title-bar">
          <span class="title-name">{{ product?.name || 'Loading...' }}</span>
        </div>

        <div class="window-body" v-if="!loading && product">

          <div class="left">
            <div class="gallery">
              <div class="image">
                <img :src="activeImageSrc" :alt="product.name" />
              </div>

              <div v-if="product.imageUrls.length > 1" class="gallery-thumbs">
                <button
                  v-for="(url, index) in product.imageUrls"
                  :key="index"
                  type="button"
                  class="gallery-thumb"
                  :class="{ 'gallery-thumb--active': index === activeImageIndex }"
                  @click="activeImageIndex = index"
                >
                  <img :src="url" :alt="''" />
                </button>
              </div>
            </div>

            <p class="stock">
              <span class="stock-line">
                Stock left:
                {{ selectedVariant ? (selectedVariant.stockLevel || selectedVariant.stock) : '—' }}
              </span>
              <span v-if="!selectedVariant" class="stock-hint">
                Select color and size first
              </span>
            </p>
          </div>

          <div class="right">
            <div class="row">
              <span class="lbl">Name:</span>
              <span class="val">{{ product.name }}</span>
            </div>

            <div class="row">
              <span class="lbl">Price:</span>
              <span class="val">${{ product.price?.toFixed(2) }}</span>
            </div>

            <div class="row">
              <span class="lbl">Quantity:</span>
              <div class="qty-ctrl">
                <button @click="qty > 0 && qty--">-</button>
                <span>{{ qty }}</span>
                <button @click="selectedVariant && qty < (selectedVariant.stockLevel || selectedVariant.stock) && qty++">+</button>
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
                >
                  {{ color }}
                </button>
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
                >
                  {{ size }}
                </button>
              </div>
            </div>
          </div>

        </div>

        <div class="button">
          <button class="cart-btn" @click="handleAddToCart">
            {{ added ? '✓ Added to Cart!' : 'Add to Cart' }}
          </button>
        </div>

        <div v-if="isManager" class="button">
          <button class="cart-btn" @click="showRestockForm = true">
            Restock
          </button>
        </div>

      </div>
    </div>

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
import { API_BASE } from '../api/base.js'
import { addToCart, getProduct } from '../api/item.js'
import RestockItemView from './RestockItemView.vue'
import { restockItem } from "../api/inventory.js"
import Header from '../views/Header.vue'

export default {
  name: 'SpecificItemView',
  components: { RestockItemView, Header },

  data() {
    const session = getSession()
    return {
      session,
      isManager: session?.accountType === 'MANAGER',
      showRestockForm: false,
      qty: 0,
      selectedColor: null,
      selectedSize: null,
      added: false,
      loading: true,
      product: null,
      activeImageIndex: 0,
    }
  },

  async mounted() {
    const id = this.$route.params.id
    try {
      const responseDto = await getProduct(id, this.session?.accountId || 1)

      const urls =
        responseDto.imageUrls && responseDto.imageUrls.length > 0
          ? responseDto.imageUrls.map((u) => `${API_BASE}${u}`)
          : []

      this.product = {
        id: responseDto.id,
        name: responseDto.name,
        price: responseDto.price,
        imageUrls: urls,
        variants: responseDto.variants || []
      }

      this.activeImageIndex = 0
    } catch (e) {
      alert("Item not found!")
    } finally {
      this.loading = false
    }
  },

  computed: {
    uniqueColors() {
      return this.product?.variants
        ? [...new Set(this.product.variants.map(v => v.color))]
        : []
    },

    availableSizesForColor() {
      if (!this.selectedColor) return []
      return this.product.variants
        .filter(v => v.color === this.selectedColor)
        .map(v => v.size)
    },

    selectedVariant() {
      return this.product?.variants?.find(
        v => v.color === this.selectedColor && v.size === this.selectedSize
      )
    },

    activeImageSrc() {
      if (!this.product?.imageUrls?.length) return ''
      return this.product.imageUrls[this.activeImageIndex]
    }
  },

  methods: {
    handleSelectColor(color) {
      this.selectedColor = color
      this.selectedSize = null
      this.qty = 0
    },

    handleSelectSize(size) {
      this.selectedSize = size
    },

    async handleAddToCart() {
      if (!this.selectedVariant) return alert("Select variant first")

      await addToCart(
        this.session.accountId,
        this.selectedVariant.id,
        this.qty
      )

      this.added = true
      setTimeout(() => (this.added = false), 2000)
    },

    async handleRestockSubmit(data) {
      const item = data[0]
      await restockItem(item.id, item.quantity, this.session.accountId)
      window.location.reload()
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}

/* IMPORTANT: only centers content, NOT header */
.page-inner {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
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
  justify-content: center; 
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
  align-items: flex-start;
  gap: 32px;
  padding: 28px 28px 16px;
}

.left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
  width: 200px;
}

.gallery {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 100%;
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
  flex-shrink: 0;
}
.image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gallery-thumbs {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  width: 100%;
  max-width: 200px;
  overflow-x: auto;
  padding-bottom: 2px;
  -webkit-overflow-scrolling: touch;
}

.gallery-thumb {
  flex: 0 0 auto;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 2px solid #ccc;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #f2f2f2;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.gallery-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.gallery-thumb:hover {
  border-color: #1209AB;
}

.gallery-thumb--active {
  border-color: #1209AB;
  box-shadow: 0 0 0 1px #1209AB;
}

.stock {
  font-size: 13px;
  color: #333;
  line-height: 1.35;
  width: 100%;
  margin: 0;
  min-height: calc(1.35em * 3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 2px;
}

.stock-line {
  font-weight: 700;
}

.stock-hint {
  font-size: 12px;
  color: #666;
  font-weight: 400;
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
  background:#FFE5F9;
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