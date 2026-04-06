<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { clearSession, getSession } from "../session/storage";
import Catalog from "./Catalog.vue";
import CartView from './CartView.vue'
import CheckoutView from './CheckoutView.vue'

const router = useRouter();
const session = ref(getSession());

const isManager = computed(() => session.value?.accountType === "MANAGER");
const isEmployee = computed(() => session.value?.accountType === "EMPLOYEE");

const activeTab = ref("shop");

function logout() {
  clearSession();
  router.replace({ name: "login" });
}

// display inventory tab only for managers
function pickTab(tab) {
  if (tab === "inventory" && !isManager.value) return;
  activeTab.value = tab;
}
</script>

<template>
  <div class="shell">
    <header class="top">
      <div class="title-block">
        <h1 class="brand">Papa's Fashionistaria</h1>
        <span class="muted" v-if="session">
          {{ session.username }} · {{ session.accountType?.toLowerCase() }}
        </span>
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
          v-if="isManager"
          type="button"
          :class="{ on: activeTab === 'inventory' }"
          @click="pickTab('inventory')"
        >
          Inventory
        </button>
        <button
            type="button"
            :class="{ on: activeTab === 'cart' }"
            @click="pickTab('cart')"
        >
          Cart
        </button>
      </nav>
      <button type="button" class="ghost" @click="logout">Log out</button>
    </header>

    <main class="main">
      <section v-show="activeTab === 'shop'">
        <Catalog />
      </section>
      <section v-show="activeTab === 'inventory'" class="card">
        <h2>Inventory</h2>
        <p class="muted">
          Manage stock and items. Managers only. (Coming soon.)
        </p>
      </section>
      <section v-show="activeTab === 'cart'">
        <CartView @go-checkout="pickTab('checkout')" />
      </section>
      <section v-show="activeTab === 'checkout'">
        <CheckoutView @order-placed="pickTab('shop')" />
      </section>
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  background: #f7f4f1;
  color: #2c2420;
}

.top {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem 1.5rem;
  padding: 1rem 1.5rem;
  background: #fff;
  border-bottom: 1px solid #e8e0db;
}

.title-block {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.brand {
  font-family: Georgia, serif;
  font-size: 1.35rem;
  font-weight: 400;
  letter-spacing: 0.06em;
  margin: 0;
}

.muted {
  color: #6b5e58;
  font-size: 0.85rem;
}

.nav-tabs {
  display: flex;
  gap: 0.5rem;
  flex: 1;
  justify-content: center;
}

.nav-tabs button {
  padding: 0.5rem 1.25rem;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: #5c524d;
  font-size: 0.95rem;
  cursor: pointer;
}

.nav-tabs button.on {
  background: #2c2420;
  color: #faf7f5;
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

.main {
  max-width: 100%;
  max-height: 100%;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 10px;
  padding: 1.75rem;
  box-shadow: 0 8px 24px rgba(40, 32, 28, 0.06);
}

.card h2 {
  margin: 0 0 0.75rem;
  font-size: 1.25rem;
  font-weight: 600;
}
</style>
