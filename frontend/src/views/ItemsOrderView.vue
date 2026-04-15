<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSession, isLoggedIn } from '../session/storage'
import { useOrderInfo } from '../api/orderInfo.js'
// IMPORT HEADER
import Header from '../views/Header.vue'
import {API_BASE} from "../api/base.js";

const route = useRoute()
const router = useRouter()
const session = getSession()

// All needed info
const { order, account, employees, loading, canCancelOrder, init, updateOrderStatus, assignEmployee } = useOrderInfo();

/**
 * Function that redirects to login if not connected and intializes order data
 * @author Wissam Abchiche
 */
onMounted(() => {
  if (!isLoggedIn()) {
    router.push('/login')
    return
  }
  const orderId = route.params.id
  init(session.accountId, orderId)
})

/**
 * List of all items of the current order.
 * Is empty does not exist
 * @author Wissam Abchiche
 */
const orderItems = computed(() => order.value?.items || [])


/**
 * Retrieve total price of the order
 * @author Wissam Abchiche
 */
const total = computed(() => {
  return order.value?.totalPrice
})

/**
 * Retrieve address of the order
 * @author Wissam Abchiche
 */
const orderAddress = computed(() => order.value?.address)

/**
 * Manually calculate the subtotal (without loyalty point)
 * @author Wissam Abchiche
 */
const subtotal = computed(() => 
  orderItems.value.reduce((sum, item) => sum + (item.itemType.generalItem.price * item.quantity), 0)
)

/**
 * Format a price into a string (with $ sign)
 * @param {number} value - Price value
 * @returns {string} formatted price string
 * @author Wissam Abchiche
 */
function formatPrice(value) {
  return `$ ${Number(value || 0).toFixed(2)}`
}

/**
 * Navigates back to the orders tab
 * @author Wissam Abchiche
 */
function goBackToOrders() {
  router.push('/app?tab=orders');
}

</script>

<template>
  <div class="page-container">
    <Header activeTab="orders" />

    <div class="checkout-page">
      <section class="checkout-main">

        <section class="card-block checkout-card">
          <div class="section-header">
            <div>
              <h2>SHIPPING ADDRESS</h2>
              <p>Shipping limited to Canada</p>
            </div>
          </div>

          <div class="address-option">
            <div class="address-content single-address">
              <div class="address-top">
                <h3>{{ session?.username ?? 'Customer' }}</h3>
              </div>
              <p>{{ orderAddress }}</p>
            </div>
          </div>
        </section>

        <section class="card-block checkout-card">
          <h2>DELIVERY METHODS</h2>

          <div class="method-row single-method-row">
            <div class="method-details">
              <div class="method-header-row">
                <h3>Standard</h3>
                <span class="method-fee">FREE</span>
              </div>
              <p>4-10 business days</p>
            </div>
          </div>

        </section>

      </section>



      <aside class="checkout-sidebar">
        <section class="card-block summary-card">
          <h2>ORDER SUMMARY</h2>
          <div class="summary-line">
            <span>Sub Total:</span>
            <span>{{ formatPrice(subtotal) }}</span>
          </div>
          
          <div class="summary-line total-line">
            <span>Total (with discount):</span>
            <span>{{ formatPrice(total) }}</span>
          </div>
        </section>

        <section class="card-block cart-card">
        <h2>ORDERED ITEMS</h2>
        <div
            v-for="item in orderItems"
            :key="item.itemType.id"
            class="mini-cart-row"
        >

          <img
              v-if="item.image && item.image.key"
              :src="`${API_BASE}/images/${item.image.key}`"
              class="mini-product-image"
              :alt="item.itemType.generalItem.name"
          />
          <div v-else class="mini-product-image placeholder-image">
            {{ item.itemType.generalItem.name.charAt(0) }}
          </div>

          <div class="mini-product-text">
            <h3>{{ item.itemType.generalItem.name }}</h3>
            <p>SIZE: {{ item.itemType.size }}</p>
            <p>COLOUR: {{ item.itemType.color }}</p>
          
          </div>
          <div class="mini-product-price">
            {{ formatPrice(item.itemType.generalItem.price * item.quantity) }}
          </div>
        </div>
      </section>

        <button type="button" class="btn" @click="goBackToOrders">BACK TO ORDERS</button>

      </aside>
    </div>
  </div>
</template>


<style scoped>
.checkout-page {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 380px);
  gap: 1.75rem;
  padding: 1.5rem;
}
.checkout-main {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.checkout-step {
  color: #8f67c7;
  font-size: 1.15rem;
  font-weight: 700;
}

.card-block {
  background: #f8eef8;
  border: 1px solid #e3d4ea;
}

.checkout-card,
.summary-card,
.cart-card {
  padding: 1.4rem;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #bfa3db;
}

.section-header h2,
.checkout-card h2,
.summary-card h2,
.cart-card h2 {
  margin: 0 0 0.45rem;
  font-size: 1.55rem;
  font-weight: 800;
}

.section-header p,
.checkout-card p,
.summary-card p,
.cart-card p {
  margin: 0.15rem 0;
  color: #5f5551;
}

.btn {
  border: 2px solid #3a33cc;
  background: #d9c0ea;
  color: #2f2ad1;
  font-weight: 800;
  cursor: pointer;
}

.btn {
  padding: 0.85rem 1.35rem;
  font-size: 1rem;
}

.address-option {
  padding-top: 1.25rem;
}

.radio-row {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.radio-row input,
.checkbox-row input {
  width: 22px;
  height: 22px;
  accent-color: #8f67c7;
  margin-top: 0.15rem;
}

.address-content,
.method-row div {
  flex: 1;
}

.single-address {
  padding-top: 0.25rem;
}

.address-textarea {
  width: 100%;
  min-height: 96px;
  padding: 0.75rem;
  border: 1px solid #d9d0c9;
  border-radius: 6px;
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
  background: #fff;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 0.75rem;
}

.address-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.address-top h3,
.method-row h3,
.mini-product-text h3 {
  margin: 0 0 0.35rem;
  font-size: 1.1rem;
  font-weight: 700;
}

.text-btn {
  padding: 0;
  border: none;
  background: transparent;
  color: #4b3b35;
  text-decoration: underline;
  cursor: pointer;
}

.checkout-error {
  margin: 1rem 0 0;
  color: #a42020;
  font-size: 0.95rem;
}

.method-row {
  margin-top: 1.2rem;
}

.single-option {
  margin-top: 1rem;
}

.checkout-sidebar {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.summary-line,
.loyalty-line {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.9rem;
  font-size: 1rem;
}

.total-line {
  margin-top: 1rem;
  padding-top: 0.9rem;
  border-top: 1px solid #bfa3db;
  font-size: 1.2rem;
  font-weight: 800;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-top: 1rem;
}

.mini-cart-row {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 0.9rem;
  align-items: start;
  margin-top: 1rem;
}

.mini-product-image {
  width: 72px;
  height: 96px;
  border-radius: 10px;
}

.placeholder-image {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f4d9e5, #cdd9f4);
  color: #3a2d27;
  font-size: 1.7rem;
  font-weight: 700;
}

.mini-product-price {
  white-space: nowrap;
  font-size: 1rem;
}

.place-order-btn {
  width: 100%;
  padding: 1rem;
  font-size: 1.8rem;
}

.secondary-btn:disabled,
.place-order-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

@media (max-width: 1100px) {
  .checkout-page {
    grid-template-columns: 1fr;
  }
}
</style>