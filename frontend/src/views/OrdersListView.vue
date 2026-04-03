<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { clearSession, getSession } from '../session/storage'

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

const orders = ref([
  {
    orderId: '26123732',
    customer: 'Alice Martin',
    employee: 'Bob',
    status: 'In Preparation',
    orderDate: '2025/03/30',
  },
  {
    orderId: '26123733',
    customer: 'Emma Stone',
    employee: 'Lina',
    status: 'Ready for Pickup',
    orderDate: '2025/03/31',
  },
  {
    orderId: '26123734',
    customer: 'Noah Kim',
    employee: 'Chris',
    status: 'Baking',
    orderDate: '2025/04/01',
  },
  {
    orderId: '26123735',
    customer: 'Sophia Chen',
    employee: 'Maya',
    status: 'Delivered',
    orderDate: '2025/04/01',
  },
  {
    orderId: '26123736',
    customer: 'Liam Patel',
    employee: 'Owen',
    status: 'Awaiting Payment',
    orderDate: '2025/04/02',
  },
])

const sortKey = ref('orderId')
const sortAsc = ref(true)
const fillerRowCount = 10

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
    <header class="topbar">
      <div class="brand-block">
        <div class="logo-wrap">
          <img
              class="logo"
              src="https://upload.wikimedia.org/wikipedia/en/thumb/6/64/Papa%27s_Pizzeria_logo.svg/512px-Papa%27s_Pizzeria_logo.svg.png"
              alt="Papa's Pizzeria"
          />
        </div>
        <div class="title-block">
          <h1 class="brand">Papa's Fashionistaria</h1>
          <span class="muted" v-if="session">
            {{ session.username }} · {{ session.accountType?.toLowerCase() }}
          </span>
        </div>
      </div>

      <nav class="nav-links">
        <button
            type="button"
            class="nav-link"
            :class="{ active: activeTab === 'shop' }"
            @click="pickTab('shop')"
        >
          Shop
        </button>

        <button
            v-if="isManager"
            type="button"
            class="nav-link"
            :class="{ active: activeTab === 'inventory' }"
            @click="pickTab('inventory')"
        >
          Inventory
        </button>

        <button
            type="button"
            class="nav-link"
            :class="{ active: activeTab === 'orders' }"
            @click="pickTab('orders')"
        >
          Orders
        </button>
      </nav>

      <div class="right-box">
        <div class="points-box">
          <div class="greeting">Hi {{ session?.username || 'Victoire' }}!</div>
          <div class="points-row">
            <span class="sparkle">✦</span>
            <span class="sparkle">✧</span>
            <span class="points">50 Points</span>
          </div>
        </div>
        <button type="button" class="ghost" @click="logout">Log out</button>
      </div>
    </header>

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

          <div v-for="n in fillerRowCount" :key="`filler-${n}`" class="tr filler">
            <div class="td">—</div>
            <div class="td">Dummy Customer {{ n }}</div>
            <div class="td">Employee {{ n }}</div>
            <div class="td">Waiting</div>
            <div class="td">2025/04/{{ String((n % 30) + 1).padStart(2, '0') }}</div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
:global(*) {
  box-sizing: border-box;
}

:global(body) {
  margin: 0;
  font-family: 'Courier New', monospace;
  background: #efefef;
}

.page {
  min-height: 100vh;
  background: #efefef;
  color: #111;
}

.topbar {
  min-height: 116px;
  background: #efddef;
  border-bottom: 3px solid #4c4c4c;
  display: grid;
  grid-template-columns: 320px 1fr 280px;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
}

.brand-block {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
}

.logo-wrap {
  width: 110px;
  height: 78px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #f8f5f5;
  border: 2px solid #c9bcbc;
  overflow: hidden;
  flex-shrink: 0;
}

.logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.title-block {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.brand {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.muted {
  color: #6b5e58;
  font-size: 0.85rem;
}

.nav-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.75rem;
}

.nav-link {
  padding: 0.55rem 1.2rem;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: #2121b8;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 6px;
}

.nav-link.active {
  background: #2c2420;
  color: #faf7f5;
  text-decoration: none;
}

.right-box {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
}

.points-box {
  text-align: right;
  color: #2121b8;
  font-weight: 700;
}

.greeting {
  font-size: 1rem;
  margin-bottom: 4px;
}

.points-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 6px;
  font-size: 0.95rem;
}

.sparkle {
  color: #e3b64f;
  font-size: 1rem;
}

.points {
  font-size: 1rem;
}

.ghost {
  padding: 0.45rem 0.9rem;
  border: 1px solid #d4ccc4;
  border-radius: 6px;
  background: #fff;
  color: #4a423e;
  font-size: 0.85rem;
  cursor: pointer;
}

.content {
  display: grid;
  grid-template-columns: 238px 1fr;
  min-height: calc(100vh - 116px);
}

.sidebar {
  background: #efefef;
  border-right: 4px solid #4c4c4c;
  padding: 26px 14px;
}

.filter-title {
  margin: 8px 0 28px;
  font-size: 30px;
  font-weight: 400;
}

.field-group {
  margin-bottom: 28px;
}

.field-group label {
  display: block;
  font-size: 25px;
  margin-bottom: 12px;
}

.field-group input {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 999px;
  padding: 0 18px;
  font-size: 18px;
  font-family: inherit;
  background: #e8e8e8;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
}

.search-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 140px;
  height: 56px;
  margin: 8px 0 36px 30px;
  border-radius: 20px;
  border: 1px solid #5e4159;
  background: linear-gradient(#f6dff0, #edd2e3);
  font-family: inherit;
  font-size: 28px;
  cursor: pointer;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 24px;
  margin: 46px 0 28px;
}

.checkbox-row input {
  width: 30px;
  height: 30px;
}

.dummy-panel {
  margin-top: 40px;
  padding: 14px;
  border: 2px dashed #bba9c4;
  background: #f6f0f7;
  font-size: 18px;
}

.dummy-panel h3 {
  margin: 0 0 10px;
  font-size: 22px;
}

.dummy-panel p {
  margin: 8px 0;
}

.table-section {
  display: flex;
  flex-direction: column;
}

.table-header,
.tr {
  display: grid;
  grid-template-columns: 1.1fr 1.1fr 1.1fr 1.1fr 1.1fr;
}

.table-header {
  background: #cbb8e5;
  min-height: 94px;
  border-bottom: 4px solid #4c4c4c;
}

.th {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-right: 4px solid #4c4c4c;
  font-size: 26px;
  font-weight: 700;
  cursor: pointer;
  padding: 8px;
}

.th:last-child,
.td:last-child {
  border-right: none;
}

.sort-arrow {
  font-size: 28px;
  transform: translateY(-2px);
}

.table-body {
  flex: 1;
}

.tr {
  min-height: 98px;
}

.tr:nth-child(odd) {
  background: #e2e2e2;
}

.tr:nth-child(even) {
  background: #ededed;
}

.td {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-right: 4px solid #4c4c4c;
  font-size: 24px;
  padding: 10px 12px;
}

.filler {
  color: #4c4c4c;
}

@media (max-width: 1100px) {
  .topbar {
    grid-template-columns: 1fr;
    padding: 12px;
  }

  .nav-links,
  .right-box {
    justify-content: center;
  }

  .th,
  .td {
    font-size: 20px;
  }
}
</style>