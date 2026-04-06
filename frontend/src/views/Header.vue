<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { clearSession, getSession } from "../session/storage";

const props = defineProps({
  activeTab: {
    type: String,
    default: 'orders'
  }
});

const router = useRouter();
const session = ref(getSession());

const isManager = computed(() => session.value?.accountType === "MANAGER");

function logout() {
  clearSession();
  router.replace({ name: "login" });
}

function pickTab(tab) {
  if (tab === "inventory" && !isManager.value) return;
  // If you aren't using nested routes, this will take you back to the AppShell
  router.push({ name: 'app', query: { tab: tab } });
}
</script>

<template>
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
        class="logout-link"
        @click="logout"
      >
        Log out
      </button>
    </nav>

    <div class="user-block">
      Hi {{ session?.username }}!
      <div class="points">✨50 Points</div>
    </div> 
  </header>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Jersey+10&display=swap');

.top {
  display: flex;
  align-items: center;
  background: #FFE5F9;
  border-bottom: 4px solid #3D3C3C;
  height: 11vh;
  width: 100%;
}

.user-block {
  height: 11vh;
  font-family: 'Jersey 10';
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
</style>