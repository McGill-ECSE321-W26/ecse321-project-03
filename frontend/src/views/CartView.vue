<script setup>
import { computed, ref, onMounted } from 'vue'
import { getCartItems, removeCartItem, updateCartItemQuantity } from '../api/cart.js'
import { getSession } from '../session/storage'

const session = getSession()
const accountId = session?.accountId
const emit = defineEmits(['go-checkout'])
const cartItems = ref([])
const loading = ref(false)
const errorMessage = ref('')

const subtotal = computed(() =>
    cartItems.value.reduce((sum, item) => sum + item.itemType.generalItem.price * item.quantity, 0),
)

const total = computed(() => subtotal.value)

const canCheckout = computed(() => !loading.value && cartItems.value.length > 0)
// for test
//const canCheckout = computed(() => !loading.value)

async function loadCartItems() {
  loading.value = true
  errorMessage.value = ''
  try {
    if (!accountId) throw new Error('No logged-in account')
    cartItems.value = await getCartItems(accountId)
  } catch (error) {
    errorMessage.value = error.message || 'Failed to load cart items.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCartItems()
})


function formatPrice(value) {
  return `$${value.toFixed(2)}`
}

function lineTotal(item) {
  return item.itemType.generalItem.price * item.quantity
}

async function removeItem(itemId) {
  try {
    if (!accountId) throw new Error('No logged-in account')
    await removeCartItem(accountId, itemId)
    await loadCartItems()
  } catch (error) {
    errorMessage.value = error.message || 'Failed to remove item.'
  }
}

async function changeQuantity(item, quantity) {
  try {
    if (!accountId) throw new Error('No logged-in account')
    await updateCartItemQuantity(accountId, item.itemType.id, quantity)
    await loadCartItems()
  } catch (error) {
    errorMessage.value = error.message || 'Failed to update quantity.'
  }
}

function goToCheckout() {
  if (!canCheckout.value) return
  emit('go-checkout')
}
</script>

<template>
  <div class="cart-page">
    <section class="cart-table card-block">
      <div class="table-header">
        <div class="products-col">PRODUCTS</div>
        <div class="price-col">PRICE</div>
        <div class="quantity-col">QUANTITY</div>
        <div class="total-col">TOTAL</div>
      </div>
      <div v-if="loading" class="empty-state">
        Loading cart items...
      </div>

      <div v-else-if="errorMessage" class="empty-state">
        {{ errorMessage }}
      </div>

      <div v-else-if="cartItems.length === 0" class="empty-state">
        Your cart is currently empty.
      </div>

  <div v-else>
    <div
        v-for="item in cartItems"
        :key="item.itemType.id"
        class="cart-row"
    >
      <div class="products-col product-info">
<!--        <img-->
<!--            v-if="item.image && item.image.key"-->
<!--            :src="`${API_BASE}/images/${item.image.key}`"-->
<!--            class="product-image"-->
<!--            :alt="item.itemType.generalItem.name"-->
<!--        />-->
<!--        <div v-else class="product-image placeholder-image">-->
<!--          {{ item.itemType.generalItem.name.charAt(0) }}-->
<!--        </div>-->
        <div class="product-image placeholder-image">
          {{ item.itemType.generalItem.name.charAt(0) }}
        </div>
        <div class="product-text">
          <h3>{{ item.itemType.generalItem.name }}</h3>
          <p>SIZE: {{ item.itemType.size }}</p>
          <p>COLOUR: {{ item.itemType.color }}</p>
          <button class="remove-btn" type="button" @click="removeItem(item.itemType.id)">
            REMOVE
          </button>
        </div>
      </div>

      <div class="price-col item-cell">
        {{ formatPrice(item.itemType.generalItem.price) }}
      </div>

      <div class="quantity-col item-cell">
        <select
            :value="item.quantity"
            class="quantity-select"
            @change="changeQuantity(item, Number($event.target.value))"
        >
          <option v-for="n in 10" :key="n" :value="n">{{ n }}</option>
        </select>
      </div>

      <div class="total-col item-cell">
        {{ formatPrice(lineTotal(item)) }}
      </div>
    </div>
  </div>
  </section>

  <aside class="summary-panel card-block">
    <button type="button" class="checkout-btn" :disabled="!canCheckout" @click="goToCheckout">
      Checkout
    </button>
    <p v-if="!loading && cartItems.length === 0" class="checkout-disabled-note">
      Add items to your cart before proceeding to checkout.
    </p>

    <div class="summary-section">
      <h2>YOUR ORDER SUMMARY</h2>
      <div class="summary-line">
        <span>Sub Total:</span>
        <span>{{ formatPrice(subtotal) }}</span>
      </div>
      <div class="summary-line">
        <span>Discount:</span>
        <span>TBD</span>
      </div>

      <div class="summary-line total-line">
        <span>Estimated Total:</span>
        <span>{{ formatPrice(total) }}</span>
      </div>

      <p class="checkout-note">
        Discounts and loyalty points are calculated at checkout.
      </p>
    </div>

    <div class="info-section">
      <h2>ADDITIONAL INFO</h2>
      <p>Shipping &amp; Handling</p>
      <p>Payment &amp; Sales Tax Info</p>
      <p>Return Policy</p>
      <p>Maximum Purchase Policy</p>
    </div>
  </aside>
  </div>
</template>

<style scoped>
.cart-page {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 360px);
  gap: 1.5rem;
  padding: 1.5rem;
  font-family: inherit;
  color: #1f1713;
}
.card-block {
  background: #fff;
  border: 1px solid #e3d4ea;
  box-shadow: 0 4px 18px rgba(44, 36, 32, 0.06);
}

.cart-table {
  background: #fff;
}

.table-header {
  display: grid;
  grid-template-columns: 2.6fr 1fr 1fr 1fr;
  align-items: center;
  background: #d8bff1;
  color: #1f1713;
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #3d3c3c;
}

.cart-row {
  display: grid;
  grid-template-columns: 2.6fr 1fr 1fr 1fr;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e3d4ea;
}

.product-info {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.product-image {
  width: 96px;
  height: 120px;
  border-radius: 10px;
  flex-shrink: 0;
  border: 1px solid #e3d4ea;
}

.placeholder-image {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f7;
  color: #3a2d27;
  font-size: 2rem;
  font-weight: 700;
}

.product-text h3 {
  margin: 0 0 0.45rem;
  font-size: 1.25rem;
  font-weight: 600;
}

.product-text p {
  margin: 0.15rem 0;
  color: #4b3b35;
  font-size: 0.95rem;
}

.remove-btn {
  margin-top: 0.8rem;
  padding: 0;
  border: none;
  background: transparent;
  color: #1209ab;
  font: inherit;
  text-decoration: underline;
  cursor: pointer;
}

.item-cell {
  font-size: 1.05rem;
  color: #342a25;
}

.quantity-select {
  padding: 0.45rem 0.75rem;
  border: 1.75px solid #3d3c3c;
  border-radius: 0;
  background: #fff;
  color: #2c2420;
  font: inherit;
}

.summary-panel {
  padding: 1.25rem;
  align-self: start;
}

.checkout-btn {
  width: 100%;
  margin-bottom: 1.2rem;
  padding: 0.95rem 1.5rem;
  border: 2px solid #8f8a8c;
  border-radius: 999px;
  background: #eccfe5;
  color: #2f2ad1;
  font-family: 'Jersey 10', sans-serif;
  font-size: 2rem;
  line-height: 1;
  font-weight: 400;
  cursor: pointer;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.18);
}

.checkout-btn:disabled {
  opacity: 1;
  cursor: not-allowed;
  background: #efddea;
  color: #8a8892;
  border-color: #9a9498;
  box-shadow: 0 3px 0 rgba(0, 0, 0, 0.12);
}

.checkout-btn:hover:not(:disabled) {
  transform: translateY(1px);
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.18);
}

.checkout-disabled-note {
  margin: -0.4rem 0 1rem;
  color: #4b3b35;
  font-size: 0.9rem;
}


.summary-section,
.info-section {
  padding-top: 1rem;
  border-top: 1px solid #e3d4ea;
}

.summary-section h2,
.info-section h2 {
  margin: 0 0 1rem;
  font-size: 1.15rem;
  font-weight: 800;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.85rem;
  font-size: 1rem;
}

.total-line {
  margin-top: 1rem;
  font-weight: 800;
  font-size: 1.15rem;
}

.info-section {
  margin-top: 1.25rem;
}

.info-section p {
  margin: 0.85rem 0;
  text-decoration: underline;
  color: #1209ab;
}

.empty-state {
  padding: 2rem 1.5rem;
  color: #4b3b35;
}

.products-col,
.price-col,
.quantity-col,
.total-col,
.item-cell,
.summary-line,
.checkout-disabled-note,
.checkout-note,
.empty-state,
.info-section p,
.remove-btn,
.quantity-select{
  font-family: inherit;
}

.remove-btn:hover,
.info-section p:hover {
  opacity: 0.8;
}

.quantity-select:focus,
.checkout-btn:focus,
.remove-btn:focus {
  outline: 2px solid #1209ab;
  outline-offset: 2px;
}


@media (max-width: 1100px) {
  .cart-page {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .table-header {
    display: none;
  }

  .cart-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .item-cell {
    display: flex;
    justify-content: space-between;
  }
}
</style>