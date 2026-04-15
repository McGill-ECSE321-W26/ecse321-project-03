<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { clearSession, getSession } from '../session/storage'
import Catalog from "./Catalog.vue";
import { getAllOrders } from '../api/order.js'
import OrderInfoView from '../views/OrderInfoView.vue';

const router = useRouter()
const session = ref(getSession())

const isManager = computed(() => session.value?.accountType === 'MANAGER')
const isEmployee = computed(() => session.value?.accountType === 'EMPLOYEE')
const isStaff = computed(() => isManager.value || isEmployee.value)

const activeTab = ref('orders')

const filters = ref({
  customerName: '',
})

const columns = computed(() => {
  const cols = [
    { key: 'orderId', label: 'Order ID' },
    { key: 'employee', label: 'Employees' },
    { key: 'status', label: 'Status' },
    { key: 'orderDate', label: 'Order Date' },
    { key: 'actions', label: 'View Order' },
  ]
  if (isStaff.value) {
    cols.splice(1, 0, { key: 'customer', label: 'Customer' });
  }
  return cols
})

const orders = ref([])
const loading = ref(false)
const error = ref(null)

const showOrderPopup = ref(false)
const selectedOrderId = ref(null)

// 3. Create a function to open the popup
function openOrderDetails(id) {
  selectedOrderId.value = id
  showOrderPopup.value = true
}

/**
 * Fetches and loads orders from the backend
 * @returns {Promise<void>}
 * @author Sonia Ly
 */
async function loadOrders() {
  loading.value = true
  error.value = null

  try {
    console.log("session:", session.value)

    const data = await getAllOrders(session.value.accountId)
    console.log("Backend response:", data)

    // Handle null, undefined, or empty list
    if (!data || data.length === 0) {
      orders.value = []
      return
    }

    console.log("First object:", data[0])

    orders.value = data.map(o => ({
      orderId: o.id,
      customer: o.customerName,
      employee: o.assigneeName,
      status: o.orderState,
      orderDate: new Date(o.orderDate).toLocaleDateString(),
    }))

    console.log("Mapped orders:", orders.value)

  } catch (err) {
    error.value = err.message
    console.error("loadOrders error:", err)
  } finally {
    loading.value = false
  }
}

/**
 * Once the page loads, call your backend
 */
onMounted(() => {
  loadOrders();

})

const sortKey = ref('orderId')
const sortAsc = ref(true)

const displayedOrders = computed(() => {
  let filtered = [...orders.value]

  if (isStaff.value && appliedFilters.value.customerName.trim()) {
    const search = appliedFilters.value.customerName.trim().toLowerCase()
    filtered = filtered.filter(order =>
        (order.customer || '').toLowerCase().includes(search)
    )
  }

  filtered.sort((a, b) => {
    const aVal = a[sortKey.value]
    const bVal = b[sortKey.value]

    if (aVal < bVal) return sortAsc.value ? -1 : 1
    if (aVal > bVal) return sortAsc.value ? 1 : -1
    return 0
  })

  return filtered
})

const appliedFilters = ref({
  customerName: '',
})

function logout() {
  clearSession()
  router.replace({ name: 'login' })
}


/**
 * Function that allows you to sort depending on a key
 * @param key
 * @author Sonia Ly
 */
function sortBy(key) {
  if (sortKey.value === key) {
    sortAsc.value = !sortAsc.value
  } else {
    sortKey.value = key
    sortAsc.value = true
  }
}

/**
 * Function that allows you to search depending on a filter value
 * @author Sonia Ly
 */
function searchOrders() {
  appliedFilters.value = { ...filters.value }
}

function handlePopupClose(shouldRefresh) {
  showOrderPopup.value = false; // Close the popup
  if (shouldRefresh === true) {
    loadOrders(); // This re-runs the GET request to update your table!
  }
}

function getStatusColor(status) {
  if (!status) return ''

  if (status === 'Ordered') return '#1E40AF'
  if (status === 'InPreparation') return '#92400E'
  if (status === 'Prepared') return '#3730A3'
  if (status === 'Delivered') return '#166534'
  if (status === 'Cancelled') return '#991B1B'

  return ''
}

const gridStyle = computed(() => {
  return {
    display: 'grid',
    // 6 columns for staff, 5 for customers
    gridTemplateColumns: isStaff.value
        ? '1.1fr 1.1fr 1.1fr 1.1fr 1.1fr 1.1fr'
        : '1.1fr 1.1fr 1.1fr 1.1fr 1.1fr'

  }
})


defineExpose({ loadOrders })

</script>

<template>

  <div class="page">


    <main class="content">
      <aside class="sidebar">
        <h3 class="filter-title" v-if="isStaff">Filter By:</h3>
        <h3 class="filter-title" v-else="isStaff">List of all the orders made by the account:</h3>

        <div v-if="isStaff" class="filter-group">
          <h4>Customer</h4>
          <label class="filter-label">Name</label>
          <input
              v-model="filters.customerName"
              type="text"
              placeholder="Alice"
          />
        </div>

        <button v-if="isStaff" class="search-btn" @click="searchOrders">
          Search
        </button>
      </aside>

      <section class="table-section">
        <div class="table-header" :style="gridStyle">
          <div
              v-for="column in columns"
              :key="column.key"
              class="th"
              @click="sortBy(column.key)"
          >
            <span>{{ column.label }}</span>
            <span v-if="column.key !== 'actions'" class="sort-arrow">
              {{ sortKey === column.key ? (sortAsc ? '▲' : '▼') : '▼' }}
            </span>
          </div>
        </div>

        <div class="table-body">
          <div v-if="displayedOrders.length === 0" class="empty-state">
            <div class="image-logo">
              <img src="../assets/images/shop_bag_icon.png" class="empty-img"/>
            </div>
            <h2 class="empty-orders-title">No orders yet!</h2>
            <p class="empty-orders-subtitle">
              You haven't placed any order.<br />
              Start browsing the shop and find something you love.
            </p>
          </div>


          <div v-else>
            <div v-for="order in displayedOrders" :key="order.orderId" class="tr" :style="gridStyle">
              <div v-for="col in columns" :key="col.key" class="td">

                <template v-if="col.key === 'status'"> <span :style="{ color: getStatusColor(order.status) }"> {{ order.status }}</span>
                </template>

                <template v-else-if="col.key === 'actions'">
                  <button class="view-btn" @click="openOrderDetails(order.orderId)">
                    View Info
                  </button>
                </template>

                <template v-else>
                  {{ order[col.key] }}
                </template>

              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    <OrderInfoView
        v-if="showOrderPopup"
        :orderId="selectedOrderId"
        @close="handlePopupClose"
    />

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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 100px;
  text-align: center;
}

.image-logo {
  background-color: #ede9fe;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 24px;
  width: 20%;
}

.empty-img {
  width: 100%;
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


.content {
  display: grid;
  grid-template-columns: 238px 1fr;
  min-height: calc(100vh - 11vh);
}

/* Sidebar inspired by catalog */
.sidebar {
  background: linear-gradient(to right, #ffe5f9 49%, #ffffff 100%);
  min-width: 160px;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 16px;
  font-family: 'Inconsolata', monospace;
}

.filter-title {
  margin: 0;
  font-size: 1.2rem;
  text-decoration: underline;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group h4 {
  text-decoration: underline;
  margin-bottom: 4px;
  font-size: 1.1rem;
}

.filter-label,
.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: monospace;
  font-size: 1rem;
}

.filter-group input[type="text"] {
  border: none;
  outline: none;
  background: white;
  padding: 6px 8px;
  font-size: 1rem;
  font-family: 'Inconsolata', monospace;
}

.checkbox-row input {
  width: 16px;
  height: 16px;
}

.search-btn {
  font-family: 'Inconsolata', monospace;
  font-size: 1rem;
  cursor: pointer;
  background: transparent;
  border: 1px solid #2c2420;
  padding: 8px 12px;
  align-self: flex-start;
}


.view-btn {
  background: linear-gradient(to top, #FFFFFF 0%, #FFEEFB 30%, #FDCEE9 80%);
  border-radius: 20px;
  width: 100px;
  height: 20px;
  border: 1px solid #000000;
  font-size: 1rem;
  cursor: pointer;
  font-family: 'Inconsolata', monospace;
}

/* Table section */
.table-section {
  display: flex;
  flex-direction: column;
  background: white;
}

.table-header,

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