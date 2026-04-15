<template>
  <div class="catalog">
    <aside class="filters">
      <h3>Filter By:</h3>


      <div class="filter-group">
        <h4>Search</h4>
        <input
            type="text"
            v-model="searchQuery"
            placeholder="Search by name or email"
            class="search-input"
        />
      </div>
    </aside>

    <main class="product-grid">
      <div v-if="loading" style="grid-column: 1/-1">Loading...</div>

      <div v-else-if="filteredAccounts.length === 0" style="grid-column: 1/-1">
        No employees found.
      </div>

      <div
          v-else
          class="product-card employee-card"
          v-for="employee in filteredAccounts"
          :key="employee.id"
      >
        <div class="employee-info">
          <p class="employee-name">{{ employee.username }}</p>
          <p>{{ employee.email }}</p>
          <p>{{ employee.address || "No address provided" }}</p>
          <p class="employee-role">{{ employee.accountType }}</p>
        </div>

        <button
            class="delete-button"
            :disabled="deletingId === employee.id || employee.id === session?.accountId"
            @click="handleDelete(employee)"
        >
          {{
            employee.id === session?.accountId
                ? "Cannot delete yourself"
                : deletingId === employee.id
                    ? "Deleting..."
                    : "Delete account"
          }}
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { API_BASE, readErrorMessage } from "../api/base.js";
import { deleteAccount } from "../api/account.js";
import { getSession } from "../session/storage";

const session = getSession();

const loading = ref(true);
const searchQuery = ref("");
const deletingId = ref(null);


const employees = ref([]); // list of al employees

/**
 * Maps backend account DTO to frontend model
 *
 * @param {Object} account - account object dto
 * @param {number} account.id
 * @param {string} account.username
 * @param {string} account.email
 * @param {string} account.address
 * @param {string} account.accountType
 * @returns {{
 *   id: number,
 *   username: string,
 *   email: string,
 *   address: string,
 *   accountType: string
 * }}
 * @author Sonia Ly
 */
function mapDto(account) {
  return {
    id: account.id,
    username: account.username,
    email: account.email,
    address: account.address,
    accountType: account.accountType,
  };
}

/**
 * Fetches all accounts
 *
 * @async
 * @returns {Promise<Array<Object>>} List of account objects
 * @throws {Error} If the API request fails
 * @author Sonia Ly
 */
async function getAllAccounts() {
  const res = await fetch(`${API_BASE}/accounts`);
  const data = await res.json().catch(() => ([]));

  if (!res.ok) {
    throw new Error(readErrorMessage(data));
  }

  return data;
}

/**
 * Computed list of accounts filtered by search
 *
 * @author Sonia Ly
 */
const filteredAccounts = computed(() => {
  return employees.value.filter((account) => {


    const query = searchQuery.value.trim().toLowerCase();
    const matchesSearch =
        query === "" ||
        account.username?.toLowerCase().includes(query) ||
        account.email?.toLowerCase().includes(query);

    return matchesSearch;
  });
});


/**
 * Loads all accounts
 *
 * @author Sonia Ly
 */
async function loadAccounts() {
  try {
    loading.value = true;
    const data = await getAllAccounts();
    employees.value = data.map(mapDto);
  } catch (error) {
    console.error("Failed to load accounts:", error);
    alert(`Could not load accounts: ${error.message}`);
  } finally {
    loading.value = false;
  }
}

/**
 * Deletes a given employee account.
 *
 * @param employee - Employee to delete
 * @author Sonia Ly
 */
async function handleDelete(employee) {
  if (!employee?.id) return;

  if (employee.id === session?.accountId) {
    alert("You cannot delete your own account.");
    return;
  }

  const confirmed = confirm(
      `Are you sure you want to delete the account "${employee.username}"?`
  );
  if (!confirmed) return;

  try {
    deletingId.value = employee.id;
    await deleteAccount(employee.id);

    employees.value = employees.value.filter(
        (currentEmployee) => currentEmployee.id !== employee.id
    );

    alert("Account deleted successfully.");
  } catch (error) {
    console.error("Failed to delete account:", error);
    alert(`Could not delete account: ${error.message}`);
  } finally {
    deletingId.value = null;
  }
}

onMounted(async () => {
  if (session?.accountType !== "MANAGER") {
    alert("Only managers can access this page.");
    loading.value = false;
    return;
  }

  await loadAccounts();
});
</script>

<style scoped>
* {
  font-family: Inconsolata, monospace;
}

.catalog {
  display: flex;
  gap: 24px;
  background-color: white;
}

.filters {
  background: linear-gradient(to right, #ffe5f9 49%, #ffffff 100%);
  min-width: 160px;
  display: flex;
  flex-direction: column;
  padding: 16px;
  padding-right: 30px;
  gap: 16px;
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

.filter-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: monospace;
  font-size: 1rem;
}

.search-input {
  width: 100%;
  padding: 8px;
  font-size: 0.95rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-sizing: border-box;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  padding: 24px;
  flex: 1;
  align-items: start;
}

.product-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 12px;
  min-height: 220px;
  padding: 20px;
  border-radius: 16px;
  background: #f7f7f7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.employee-card {
  align-items: flex-start;
}

.employee-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.employee-info p {
  margin: 0;
  font-size: 1rem;
}

.employee-name {
  font-size: 1.2rem;
  font-weight: bold;
}

.employee-role {
  text-transform: uppercase;
  font-size: 0.95rem;
}

.delete-button {
  padding: 10px 14px;
  border: none;
  border-radius: 10px;
  background-color: #d9534f;
  color: white;
  font-size: 0.95rem;
  cursor: pointer;
}

.delete-button:hover {
  opacity: 0.9;
}

.delete-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

h3 {
  margin: 0;
  font-size: 1.2rem;
}
</style>