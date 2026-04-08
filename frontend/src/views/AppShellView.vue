<script setup>
import { onMounted, computed, ref } from "vue";
import { useRouter } from "vue-router";
import { clearSession, getSession } from "../session/storage";
import Catalog from "./Catalog.vue";
import CartView from './CartView.vue'
import CheckoutView from './CheckoutView.vue'
import AccountDetailView from "./AccountDetailView.vue";
import OrdersListView from "./OrdersListView.vue";
import { getAccountById } from "../api/account.js";

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
    activeTab.value = tab;

}

const points = ref(0);
async function findNumberOfPoints() {
  if (session.value.accountId) {
    try {
      const account = await getAccountById(session.value.accountId);
      points.value = account?.loyaltyPts;
    } catch (error) {
      console.error(err);
      window.alert(error.message)
    }
  }
}
onMounted(() => {
  findNumberOfPoints();

  const urlParams = new URLSearchParams(window.location.hash.split('?')[1]);
  const tab = urlParams.get('tab');
  if (tab) {
    activeTab.value = tab;
  }
});

function refreshSession() {
  session.value = getSession();
  findNumberOfPoints();
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



        <button type="button" class="logout-link" @click="logout">Log out</button>
      </nav>

      <div class="user-block">
        <button
          type="button"
          class="user-profile-btn"
          :class="{ on: activeTab === 'account' }"
          aria-label="Open account details"
          :aria-current="activeTab === 'account' ? 'page' : undefined"
          @click="pickTab('account')"
        >
          <img src="../assets/images/user.png" alt="" class="user-icon-img" width="28" height="28" />
        </button>
        <div class="user-text">
          Hi {{ session?.username }}!
          <div class="points">✨{{ points }} Points</div>
        </div>
      </div>
    </header>

    <main class="main">
      <section v-show="activeTab === 'shop'">
        <Catalog />
      </section>


      <section v-show="activeTab === 'orders'">
        <OrdersListView />
      </section>

      <section v-show="activeTab === 'cart'">
        <CartView @go-checkout="pickTab('checkout')" />
      </section>


      <section v-show="activeTab === 'account'">
        <AccountDetailView @session-updated="refreshSession" />
      </section>
      <section v-show="activeTab === 'checkout'">
        <CheckoutView @order-placed="pickTab('shop')" />
      </section>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jersey+10&display=swap');
.shell {
  min-height: 100vh;
  background: #f7f4f1;
  color: #2c2420;
}

.top {
  display: flex;
  align-items: center;
  background: #FFE5F9;
  border-bottom: 4px solid #3D3C3C;
  height: 11vh;

}
.user-block {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 11vh;
  font-family: 'Jersey 10';
  color: #1209ab;
  font-size: 4vh;
  margin-right: 20px;
  margin-top: 20px;
  line-height: 1;
}

.user-profile-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 50%;
}

.user-profile-btn:hover {
  background: rgba(18, 9, 171, 0.08);
}

.user-profile-btn:focus-visible {
  outline: 2px solid #1209ab;
  outline-offset: 2px;
}

.user-profile-btn.on {
  background: rgba(18, 9, 171, 0.12);
  box-shadow: inset 0 0 0 2px #1209ab;
}

.user-icon-img {
  display: block;
  width: clamp(26px, 3.5vh, 36px);
  height: clamp(26px, 3.5vh, 36px);
  object-fit: contain;
}

.user-text {
  text-align: right;
}

.points {
  margin-top: 4px;
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
  font-family: 'Jersey 10';
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
  font-family: 'Jersey 10';
  color: #1209AB;
  font-size: 2vh;
  cursor: pointer;

  background: #FFFFFF; 
  border: 2px solid #1209AB;
  outline: none;
  padding: 4px 8px;
}


.main {
  max-width: 100%;
  max-height: 100%;
  margin: 0 auto;
}


</style>
