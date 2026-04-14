/* Vue file (JavaScript + HTML + CSS) for the specific item view. 
This is a child of the catalog
Author: Maria Klinski */

<template>
  <div class="page">

    <Header activeTab="shop" />

    <div class="page-inner">

      <div class="window">

        <div class="title-bar">
          <span class="title-name">{{ product?.name || 'Loading...' }}</span>
      <!-- wait for the product to load -->
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
                class="gallery-thumb"
                :class="{ 'gallery-thumb--active': index === activeImageIndex }"
                :aria-label="`Show image ${index + 1} of ${product.imageUrls.length}`"
                :aria-pressed="index === activeImageIndex"
                @click="activeImageIndex = index"
              >
                <img :src="url" alt="" />
              </button>
            </div>
          </div>

          <!-- Stock count -->
          <p class="stock">
            <span class="stock-line">
              Stock left:
              {{ selectedVariant ? (selectedVariant.stockLevel || selectedVariant.stock) : '—' }}
            </span>
            <span v-if="!selectedVariant" class="stock-hint">Select color and size first</span>
          </p>
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
          <div class="row">
            <span class="lbl">Quantity:</span>
            <div class="qty-ctrl">
              <!-- Don't go below 0 -->
              <button @click="qty > 0 && qty--">-</button>
              <span>{{ qty }}</span>
              <!-- Don't go above available stock -->
              <button @click="selectedVariant && qty < (selectedVariant.stockLevel || selectedVariant.stock) && qty++">+</button>
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

    <!-- restock form for manager -->
    <RestockItemView
      :visible="showRestockForm"
      :product="product"
      @close="showRestockForm = false"
      @restock="handleRestockSubmit"
    />
    <!-- edit form for manager -->
    <EditItemView
      :visible="showEditForm"
      :product="product"
      @close="showEditForm = false"
      @update="handleEditSubmit"
    />
  </div>
</template>

<script>
import { getSession } from '../session/storage'
import { API_BASE } from '../api/base.js'
import { addToCart, getProduct, updateItem, deleteItem } from '../api/item.js'
import RestockItemView from './RestockItemView.vue'
import { restockItem } from "../api/inventory.js"
import Header from '../views/Header.vue'

export default {
  name: 'SpecificItemView',
  components: { RestockItemView, Header },
import EditItemView from './EditItemView.vue'
import { restockItem } from '../api/inventory.js'

export default {
  name: 'SpecificItemView',
  components: { RestockItemView, EditItemView },

  data() {
    const session = getSession()
    return {
      session,
      isManager: session?.accountType === 'MANAGER', // gates manager UI
      showRestockForm: false,
      showEditForm: false,
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
        responseDto.imageUrls?.length
          ? responseDto.imageUrls.map((u) => `${API_BASE}${u}`)
          : ['../assets/images/placeholder.jpg']

      this.product = {
        id:        responseDto.id       ?? responseDto.item?.id,
        name:      responseDto.name     ?? responseDto.item?.name,
        price:     responseDto.price    ?? responseDto.item?.price,
        tag:       responseDto.tag      ?? responseDto.item?.tag ?? '',
        imageUrls: urls,
        variants:  responseDto.variants || [],
      }
    } catch (error) {
      console.error('Failed to load specific item:', error)
      alert('Item not found!')
    } finally {
      this.loading = false
    }
  },

  computed: {
    uniqueColors() {
      if (!this.product?.variants) return []
      return [...new Set(this.product.variants.map((v) => v.color))]
    },

    // Sizes available for whichever color is currently selected
    availableSizesForColor() {
      if (!this.product?.variants || !this.selectedColor) return []
      return this.product.variants
        .filter((v) => v.color === this.selectedColor)
        .map((v) => v.size)
    },

    // matching the current color + size selection
    selectedVariant() {
      if (!this.product || !this.selectedColor || !this.selectedSize) return null
      return this.product.variants.find(
        (v) => v.color === this.selectedColor && v.size === this.selectedSize
      )
    },

    activeImageSrc() {
      if (!this.product?.imageUrls?.length) return '../assets/images/placeholder.jpg'
      const i = Math.min(this.activeImageIndex, this.product.imageUrls.length - 1)
      return this.product.imageUrls[i]
    },
  },

  watch: {
    // Reset to first image when navigating to a different product
    '$route.params.id'() {
      this.activeImageIndex = 0
    },
  },

  methods: {
    // Switching color clears size and quantity
    handleSelectColor(color) {
      this.selectedColor = color
      this.selectedSize  = null
      this.qty           = 0
    },

    handleSelectSize(size) {
      this.selectedSize = size
      // Wait for update
      this.$nextTick(() => {
        const stock = this.selectedVariant?.stockLevel || this.selectedVariant?.stock || 0
        if (this.qty > stock)                          this.qty = stock  // clamp down
        else if (this.qty === 0 && stock > 0)          this.qty = 1      // auto-set to 1
      })
    },

    async handleAddToCart() {
      if (!this.selectedColor || !this.selectedSize)
        return alert('Please select both a color and a size!')
      if (this.qty === 0)
        return alert('Please select a valid quantity!')
      const stock = this.selectedVariant.stockLevel || this.selectedVariant.stock
      if (this.qty > stock)
        return alert('Not enough stock for this variant!')

      try {
        await addToCart(this.session?.accountId || 1, this.selectedVariant.id, this.qty)
        this.added = true
        setTimeout(() => (this.added = false), 2000) // reset button text after 2s
      } catch (error) {
        alert(error.message)
      }
    },

    async handleRestockSubmit(restockData) {
      try {
        const item = restockData[0]
        await restockItem(item.id, item.quantity, this.session?.accountId)
        alert('Restock successful!')
        window.location.reload() // reload to reflect new stock quantity
      } catch (error) {
        alert(error.message || 'Failed to restock item.')
      }
    },

    async handleEditSubmit(dto) {
      try {
        const saved = await updateItem(this.session?.accountId || 1, this.product.id, {
          ...dto,
          variants: this.product.variants,
        })

  
        this.product.name  = saved.name  ?? dto.name
        this.product.price = saved.price ?? dto.price
        this.product.tag   = saved.tag   ?? dto.tag

        if (saved.imageUrls?.length) {
          this.product.imageUrls = saved.imageUrls.map((u) => `${API_BASE}${u}`)
          this.activeImageIndex  = 0
        }

        alert('Item updated successfully!')
      } catch (error) {
        alert(error.message || 'Failed to update item.')
      }
    },

    async handleDeleteItem() {
      if (!confirm(`Delete "${this.product.name}"? This cannot be undone.`)) return
      try {
        await deleteItem(this.session?.accountId || 1, this.product.id)
        this.$router.back()
      } catch (error) {
        alert(error.message || 'Failed to delete item.')
      }
    },
  },
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
  background: #FFE5F9;
  border: 0.5px solid #000000;
  border-radius: 20px;
  padding: 6px 14px;
  font-family: 'Inconsolata', monospace;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 2px 2px 0 #aaa;
  transition: opacity 0.15s, transform 0.1s;
}
.back-btn:hover  { opacity: 0.85; transform: translate(-1px,-1px); box-shadow: 3px 3px 0 #aaa; }
.back-btn:active { transform: translate(1px,1px); box-shadow: 1px 1px 0 #aaa; }

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
.image img { width: 100%; height: 100%; object-fit: cover; }

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
.gallery-thumb img { width: 100%; height: 100%; object-fit: cover; display: block; }
.gallery-thumb:hover { border-color: #1209AB; }
.gallery-thumb--active { border-color: #1209AB; box-shadow: 0 0 0 1px #1209AB; }

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
.stock-line { font-weight: 700; }
.stock-hint { font-size: 12px; color: #666; font-weight: 400; }

.right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.row { display: flex; align-items: center; gap: 12px; }

.lbl { font-size: 16px; font-weight: 700; color: #222; white-space: nowrap; }

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
  width: 28px; height: 30px;
  background: none; border: none;
  font-size: 16px;
  font-family: 'Inconsolata', monospace;
  cursor: pointer; color: #333;
}
.qty-ctrl button:hover { background: #ddd; border-radius: 50%; }
.qty-ctrl span { width: 32px; text-align: center; font-size: 14px; }

.colors-block { display: flex; flex-direction: column; gap: 8px; }

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
.color-btn.active { background: #1209AB; color: #fff; border-color: #1209AB; }

/* Add to Cart button */
.button {
  padding: 8px 28px 16px;
  display: flex;
  justify-content: center;
}

.cart-btn {
  background: #FFE5F9;
  border: 0.5px solid #000000;
  border-radius: 28px;
  padding: 12px 56px;
  font-family: 'Inconsolata', monospace;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 3px 3px 0 #8a7a86;
  transition: all 0.1s ease;
}
.cart-btn:hover  { transform: translate(-1px, -1px); box-shadow: 4px 4px 0 #8a7a86; }
.cart-btn:active { transform: translate(1px, 1px);   box-shadow: 1px 1px 0 #8a7a86; }

/* Manager button row */
.manager-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 0 28px 24px;
}

.mgr-btn {
  border-radius: 24px;
  padding: 10px 24px;
  font-family: 'Inconsolata', monospace;
  font-size: 16px;
  cursor: pointer;
  border: 2px solid #888;
  box-shadow: 2px 2px 0 #aaa;
  transition: background 0.15s, transform 0.1s;
}
.mgr-btn:hover  { transform: translate(-1px, -1px); box-shadow: 3px 3px 0 #aaa; }
.mgr-btn:active { transform: translate(1px, 1px);   box-shadow: 1px 1px 0 #aaa; }

.restock-btn { background: #e8f5e9; border-color: #888; }
.restock-btn:hover { background: #c8e6c9; }

.edit-btn { background: #f8bbd0; border-color: #888; }
.edit-btn:hover { background: #f48fb1; }

.delete-btn { background: #ffcdd2; border-color: #888; }
.delete-btn:hover { background: #ef9a9a; }
</style>