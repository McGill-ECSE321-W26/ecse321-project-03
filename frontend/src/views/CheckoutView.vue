<script setup>
import { computed, ref, onMounted } from 'vue'
import { getSession } from '../session/storage'
import {
  placeOrder as placeOrderRequest,
  getLoyaltyPoints,
} from '../api/checkout.js'
import { getAccountById, updateAddress } from '../api/account.js'
import { getCartItems } from '../api/cart.js'
import { API_BASE } from '../api/base.js'


const emit = defineEmits(['order-placed'])
const session = getSession()
const accountId = session?.accountId

const subtotal = computed(() =>
    cartItems.value.reduce((sum, item) => sum + item.itemType.generalItem.price * item.quantity, 0),
)
const availablePoints = ref(0)
const billingSameAsShipping = ref(true)

const selectedDeliveryDate = ref('')

/**
 * Formats a Date object into YYYY-MM-DD format for the date input field.
 *
 * @param {Date} date the date to format
 * @returns {string} the formatted date string
 * @author Yani Zhang
 */
function formatDateForInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Computes the earliest delivery date allowed for checkout.
 * The minimum delivery date is set to 2 days after today.
 *
 * @returns {string} the minimum allowed delivery date in YYYY-MM-DD format
 * @author Yani Zhang
 */
function getMinimumDeliveryDate() {
  const minDate = new Date()
  minDate.setDate(minDate.getDate() + 2)
  return formatDateForInput(minDate)
}

const minimumDeliveryDate = getMinimumDeliveryDate()
selectedDeliveryDate.value = minimumDeliveryDate


const currentAddress = ref('')
const isEditingAddress = ref(false)
const editedAddress = ref('')
const savingAddress = ref(false)

const addressError = ref('')
const loyaltyError = ref('')

const cartItems = ref([])

const discountRate = computed(() => {
  const pts = availablePoints.value
  if (pts < 5) return 0
  if (pts < 10) return 0.01
  if (pts < 15) return 0.02
  if (pts < 20) return 0.05
  if (pts < 35) return 0.1
  return 0.15
})
const discount = computed(() => subtotal.value * discountRate.value)
const total = computed(() => Math.max(subtotal.value - discount.value, 0))

/**
 * Loads the loyalty points of the currently logged-in account.
 *
 * @returns {Promise<void>}
 * @author Yani Zhang
 */
async function loadLoyaltyPoints() {
  try {
    if (!accountId) throw new Error('No logged-in account found.')
    loyaltyError.value = ''
    availablePoints.value = await getLoyaltyPoints(accountId)
  } catch (error) {
    loyaltyError.value = error.message || 'Failed to load loyalty points.'
  }
}

/**
 * Loads the current shipping address of the logged-in account.
 *
 * @returns {Promise<void>}
 * @author Yani Zhang
 */
async function loadCurrentAddress() {
  try {
    if (!accountId) throw new Error('No logged-in account found.')
    addressError.value = ''
    const data = await getAccountById(accountId)
    currentAddress.value = data.address ?? ''
    editedAddress.value = currentAddress.value
  } catch (error) {
    addressError.value = error.message || 'Failed to load account address.'
  }
}

/**
 * Opens the address editing mode and pre-fills the textarea.
 *
 * @author Yani Zhang
 */
function startEditingAddress() {
  editedAddress.value = currentAddress.value
  isEditingAddress.value = true
}

/**
 * Cancels address editing and restores the original address value.
 *
 * @author Yani Zhang
 */
function cancelEditingAddress() {
  editedAddress.value = currentAddress.value
  isEditingAddress.value = false
}

/**
 * Saves the updated shipping address for the logged-in account.
 *
 * @returns {Promise<void>}
 * @author Yani Zhang
 */
async function saveAddress() {
  try {
    if (!accountId) throw new Error('No logged-in account found.')
    savingAddress.value = true
    addressError.value = ''
    await updateAddress(accountId, editedAddress.value)

    currentAddress.value = editedAddress.value
    isEditingAddress.value = false
  } catch (error) {
    addressError.value = error.message || 'Failed to update address.'
  } finally {
    savingAddress.value = false
  }
}

/**
 * Loads all cart items to display in the checkout summary.
 *
 * @returns {Promise<void>}
 * @author Yani Zhang
 */
async function loadCheckoutCartItems() {
  try {
    if (!accountId) throw new Error('No logged-in account found.')
    const data = await getCartItems(accountId)
    // Store the cart items for checkout summary and total calculation
    cartItems.value = data
  } catch (error) {
    addressError.value = error.message || 'Failed to load cart items.'
  }
}

/**
 * Once the page loads, fetch the loyalty points, saved address, and cart items.
 */
onMounted(() => {
  loadLoyaltyPoints()
  loadCurrentAddress()
  loadCheckoutCartItems()
})

/**
 * Formats a numeric value as a price string.
 *
 * @param {number} value the price value to format
 * @returns {string} the formatted price string
 * @author Yani Zhang
 */
function formatPrice(value) {
  return `$ ${value.toFixed(2)}`
}

/**
 * Places an order using the selected delivery date and current shipping address.
 *
 * @returns {Promise<void>}
 * @author Yani Zhang
 */
async function placeOrder() {
  try {
    if (!accountId) throw new Error('No logged-in account found.')
    // Send the checkout request to the backend with delivery date and address
    await placeOrderRequest({
      customerId: accountId,
      delivDate: new Date(`${selectedDeliveryDate.value}T12:00:00`).toISOString(),
      address: currentAddress.value
    })
    window.alert('Order placed successfully.')
    emit('order-placed')
  } catch (error) {
    console.error(error)
    window.alert(error.message || 'Failed to place order.')
  }
}
</script>

<template>
  <div class="checkout-page">
    <section class="checkout-main">
      <section class="card-block checkout-card">
        <div class="section-header">
          <div>
            <h2>SHIPPING ADDRESS</h2>
            <p>Shipping limited to Canada</p>
          </div>
        </div>

        <p v-if="addressError" class="checkout-error">{{ addressError }}</p>
        <div class="address-option">
          <div v-if="!isEditingAddress" class="address-content single-address">
            <div class="address-top">
              <h3>{{ session?.username ?? 'Current User' }}</h3>
              <button type="button" class="text-btn" @click="startEditingAddress">Edit</button>
            </div>
            <p>{{ currentAddress || 'No address on file.' }}</p>
          </div>

          <div v-else class="address-content single-address">
            <div class="address-top">
              <h3>{{ session?.username ?? 'Current User' }}</h3>
            </div>
            <textarea v-model="editedAddress" class="address-textarea"></textarea>
            <div class="address-actions">
              <button type="button" class="secondary-btn" @click="cancelEditingAddress">Cancel</button>
              <button type="button" class="secondary-btn" :disabled="savingAddress" @click="saveAddress">
                {{ savingAddress ? 'Saving…' : 'Save' }}
              </button>
            </div>
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
            <p>2-10 business days</p>
          </div>
        </div>

        <div class="delivery-date-section">
          <label for="delivery-date" class="delivery-date-label">Delivery Date</label>
          <input
              id="delivery-date"
              v-model="selectedDeliveryDate"
              class="delivery-date-input"
              type="date"
              :min="minimumDeliveryDate"
          />
          <p class="delivery-date-note">Please choose a date at least 2 days after today.</p>
        </div>

      </section>
    </section>

    <aside class="checkout-sidebar">
      <section class="card-block summary-card">
        <h2>YOUR ORDER SUMMARY</h2>
        <div class="summary-line">
          <span>Sub Total:</span>
          <span>{{ formatPrice(subtotal) }}</span>
        </div>
        <div class="summary-line">
          <span>Shipping:</span>
          <span>FREE</span>
        </div>
        <div class="summary-line">
          <span>Discount:</span>
          <span>-{{ formatPrice(discount) }}</span>
        </div>
        <div class="summary-line total-line">
          <span>Total:</span>
          <span>{{ formatPrice(total) }}</span>
        </div>
      </section>

      <section class="card-block loyalty-card">
        <h2>LOYALTY POINTS</h2>
        <p v-if="loyaltyError" class="checkout-error">{{ loyaltyError }}</p>
        <div class="loyalty-line">
          <span>Loyalty Points You Have!</span>
          <span>{{ availablePoints }} points</span>
        </div>
        <div class="loyalty-line">
          <span>Discount Rate:</span>
          <span>{{ Math.round(discountRate * 100) }}% (VIP estimate)</span>
        </div>
        <div class="loyalty-line">
          <span>Discount:</span>
          <span>-{{ formatPrice(discount) }}</span>
        </div>
      </section>

      <section class="card-block cart-card">
        <h2>SHOPPING CART</h2>
        <div
            v-for="item in cartItems"
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

      <button type="button" class="place-order-btn" @click="placeOrder">PLACE ORDER</button>
    </aside>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jersey+10&display=swap');

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
.loyalty-card,
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
.loyalty-card h2,
.cart-card h2 {
  margin: 0 0 0.45rem;
  font-size: 1.55rem;
  font-weight: 800;
}

.section-header p,
.checkout-card p,
.summary-card p,
.loyalty-card p,
.cart-card p {
  margin: 0.15rem 0;
  color: #5f5551;
}

.secondary-btn,
.place-order-btn {
  border: 2px solid #3a33cc;
  background: #d9c0ea;
  color: #2f2ad1;
  font-weight: 800;
  cursor: pointer;
}

.secondary-btn {
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

.single-method-row {
  display: block;
}

.method-row {
  margin-top: 1.2rem;
}

.delivery-date-section {
  margin-top: 1.25rem;
  padding-top: 1rem;
  border-top: 1px solid #bfa3db;
}

.delivery-date-label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 1rem;
  font-weight: 700;
}

.delivery-date-input {
  width: 100%;
  max-width: 260px;
  padding: 0.75rem;
  border: 1px solid #d9d0c9;
  border-radius: 6px;
  font-size: 1rem;
  font-family: inherit;
  background: #fff;
}

.delivery-date-note {
  margin-top: 0.5rem;
  font-size: 0.95rem;
  color: #5f5551;
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
  font-size: 2.5rem;
  font-family: 'Jersey 10', sans-serif;
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