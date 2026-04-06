<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { clearSession, getSession } from '../session/storage'
import Catalog from "./Catalog.vue";
import { getAllOrders } from '../api/order.js'

const router = useRouter()
const session = ref(getSession())

const isManager = computed(() => session.value?.accountType === 'MANAGER')
const isEmployee = computed(() => session.value?.accountType === 'EMPLOYEE')

const activeTab = ref('orders')

const filters = ref({
  email: 'alice@example.com',
  customerId: 'CUST-1042',
  showPastOrders: false,
})

const columns = [
  { key: 'orderId', label: 'Order ID' },
  { key: 'customer', label: 'Customer' },
  { key: 'employee', label: 'Employees' },
  { key: 'status', label: 'Status' },
  { key: 'orderDate', label: 'Order Date' },
]

const orders = ref([])
const loading = ref(false)
const error = ref(null)


/**
 * Function to load the orders from the backend
 * @author Sonia Ly
 */
async function loadOrders() {
  loading.value = true
  error.value = null

  try {
    const data = await getAllOrders()

    console.log("Backend response: ", data)

    // 🔥 Adapt this depending on your backend response shape
    orders.value = data.map(o => ({
      orderId: o.id,
      customer: o.customerName,
      employee: o.employeeName,
      status: o.status,
      orderDate: o.orderDate,
    }))

  } catch (err) {
    error.value = err.message
    console.error(err)
  } finally {
    loading.value = false
  }
}

/**
 * Once the page boards, call your backend
 */
onMounted(() => {
  loadOrders()
})

const sortKey = ref('orderId')
const sortAsc = ref(true)

const displayedOrders = computed(() => {
  const sorted = [...orders.value].sort((a, b) => {
    const aVal = a[sortKey.value]
    const bVal = b[sortKey.value]

    if (aVal < bVal) return sortAsc.value ? -1 : 1
    if (aVal > bVal) return sortAsc.value ? 1 : -1
    return 0
  })

  return sorted
})

function logout() {
  clearSession()
  router.replace({ name: 'login' })
}

function pickTab(tab) {
  if (tab === 'inventory' && !isManager.value) {
    return activeTab.value = tab
  } else if (tab === 'orders') {
    router.push({ name: 'orderList' })
  } else {
   router.push({ name: 'appShell' })
  }
}

function sortBy(key) {
  if (sortKey.value === key) {
    sortAsc.value = !sortAsc.value
  } else {
    sortKey.value = key
    sortAsc.value = true
  }
}

function searchOrders() {
  console.log('Dummy search triggered with:', filters.value)
}
</script>

<template>
  <div class="page">
    <header class="top">
      <div class="logo-block">
        <img src="../assets/images/logo.png" class="logo-img" />
      </div>

      <nav class="nav-tabs">
        <button
            type="button"
            :class="{ on: activeTab === 'shop' }"
            @click="pickTab('shop')"
        >
          Shop
        </button>

        <button
            type="button"
            :class="{ on: activeTab === 'orders' }"
            @click="pickTab('orders')"
        >
          Orders
        </button>

        <button
            type="button"
            :class="{ on: activeTab === 'cart' }"
            @click="pickTab('cart')"
        >
          Cart
        </button>

        <button
            v-if="isManager"
            type="button"
            :class="{ on: activeTab === 'inventory' }"
            @click="pickTab('inventory')"
        >
          Inventory
        </button>

        <button
            type="button"
            :class="logout-link"
            @click="logout">Log out
        </button>

      </nav>

      <div class="user-block">
        Hi {{ session?.username }}!
        <div class="points">✨50 Points</div>
      </div>
    </header>

    <main class="main">
      <section v-show="activeTab === 'shop'">
        <Catalog />
      </section>

      <section v-show="activeTab === 'cart'">
        <Cart />
      </section>

      <section v-show="activeTab === 'inventory'">
        <Inventory />
      </section>
    </main>

    <main class="content">
      <aside class="sidebar">
        <h2 class="filter-title">Filter By:</h2>

        <div class="field-group">
          <label>Customer email:</label>
          <input v-model="filters.email" type="text" placeholder="alice@example.com" />
        </div>

        <div class="field-group">
          <label>Customer ID:</label>
          <input v-model="filters.customerId" type="text" placeholder="CUST-1042" />
        </div>

        <button class="search-btn" @click="searchOrders">Search</button>

        <label class="checkbox-row">
          <input v-model="filters.showPastOrders" type="checkbox" />
          <span>Show past orders</span>
        </label>

        <div class="dummy-panel">
          <h3>Dummy Notes</h3>
          <p>Current employee: Bob Rossi</p>
          <p>Shift: Morning Shift</p>
          <p>Location: Downtown Branch</p>
        </div>
      </aside>

      <section class="table-section">
        <div class="table-header">
          <div
              v-for="column in columns"
              :key="column.key"
              class="th"
              @click="sortBy(column.key)"
          >
            <span>{{ column.label }}</span>
            <span class="sort-arrow">▼</span>
          </div>
        </div>

        <div class="table-body">
          <div v-for="order in displayedOrders" :key="order.orderId" class="tr">
            <div class="td">{{ order.orderId }}</div>
            <div class="td">{{ order.customer }}</div>
            <div class="td">{{ order.employee }}</div>
            <div class="td">{{ order.status }}</div>
            <div class="td">{{ order.orderDate }}</div>
          </div>

        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jersey+10&family=Inconsolata:wght@400;500;600;700&display=swap');

:global(*) {
  box-sizing: border-box;
}

:global(body) {
  margin: 0;
  background: #efefef;
}

.page {
  min-height: 100vh;
  background: #f7f4f1;
  color: #2c2420;
  font-family: 'Inconsolata', monospace;
}

/* ===== HEADER ===== */
.top {
  display: flex;
  align-items: center;
  background: #FFE5F9;
  border-bottom: 4px solid #3D3C3C;
  height: 11vh;
}

.user-block {
  height: 11vh;
  font-family: 'Jersey 10', monospace;
  color: #1209AB;
  font-size: 4vh;
  margin-right: 20px;
  margin-top: 20px;
  line-height: 1;
}

.points {
  margin-left: -20px;
}

.logo-block {
  height: 100%;
  display: flex;
  align-items: center;
}

.logo-img {
  height: 90%;
  width: auto;
  object-fit: contain;
  margin-left: 20px;
}

.nav-tabs {
  display: flex;
  gap: 100px;
  flex: 1;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.nav-tabs button {
  font-family: 'Jersey 10', monospace;
  color: #1209AB;
  font-size: 4vh;
  cursor: pointer;
  background: transparent;
  border: none;
  outline: none;
  padding: 4px 8px;
}

.nav-tabs button.on {
  background: transparent;
  border-bottom: 3px solid #1209AB;
}

.logout-link {
  font-family: 'Jersey 10', monospace;
  color: #1209AB;
  font-size: 2vh;
  cursor: pointer;
  background: #FFFFFF;
  border: 2px solid #1209AB;
  outline: none;
  padding: 4px 8px;
}

/* ===== ORDERS PAGE ===== */
.main {
  max-width: 100%;
  max-height: 100%;
  margin: 0 auto;
}

.content {
  display: grid;
  grid-template-columns: 238px 1fr;
  min-height: calc(100vh - 11vh);
}

/* Sidebar inspired by catalog */
.sidebar {
  background: linear-gradient(to right, #ffe5f9 49%, #ffffff 100%);
  border-right: 4px solid #4c4c4c;
  padding: 24px 16px;
  font-family: 'Inconsolata', monospace;
}

.filter-title {
  margin: 8px 0 28px;
  font-size: 1.6rem;
  font-weight: 700;
  text-decoration: underline;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 28px;
}

.field-group label {
  font-size: 1rem;
  font-weight: 600;
}

.field-group input {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 999px;
  padding: 0 18px;
  font-size: 1rem;
  font-family: 'Inconsolata', monospace;
  background: #e8e8e8;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
}

.search-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 140px;
  height: 48px;
  margin: 8px 0 28px 0;
  border-radius: 16px;
  border: 1px solid #5e4159;
  background: #f7f7f7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
  font-family: 'Inconsolata', monospace;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1rem;
  margin: 24px 0 28px;
}

.checkbox-row input {
  width: 18px;
  height: 18px;
}

.dummy-panel {
  margin-top: 32px;
  padding: 14px;
  border-radius: 16px;
  background: #f7f7f7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  font-size: 0.95rem;
}

.dummy-panel h3 {
  margin: 0 0 10px;
  font-size: 1.1rem;
}

.dummy-panel p {
  margin: 8px 0;
}

/* Table section */
.table-section {
  display: flex;
  flex-direction: column;
  background: white;
}

.table-header,
.tr {
  display: grid;
  grid-template-columns: 1.1fr 1.1fr 1.1fr 1.1fr 1.1fr;
}

.table-header {
  background: #cbb8e5;
  min-height: 84px;
  border-bottom: 4px solid #4c4c4c;
}

.th {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-right: 4px solid #4c4c4c;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  padding: 8px;
  text-align: center;
}

.th:last-child,
.td:last-child {
  border-right: none;
}

.sort-arrow {
  font-size: 1rem;
}

.table-body {
  flex: 1;
}

.tr {
  min-height: 88px;
}

.tr:nth-child(odd) {
  background: #eae8e8;
}

.tr:nth-child(even) {
  background: #f4f4f4;
}

.td {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-right: 4px solid #4c4c4c;
  font-size: 1rem;
  padding: 10px 12px;
}

@media (max-width: 1100px) {
  .nav-tabs {
    gap: 30px;
  }

  .content {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 4px solid #4c4c4c;
  }

  .th,
  .td {
    font-size: 0.9rem;
  }
}
</style>