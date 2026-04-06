<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { clearSession, getSession } from "../session/storage";
import Catalog from "./Catalog.vue";

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

      <section v-show="activeTab === 'orders'">
        <Orders />
      </section>
      <section v-show="activeTab === 'cart'">
        <Cart />
      </section>

      <section v-show="activeTab === 'inventory'">
         <Inventory />      
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


.main {
  max-width: 100%;
  max-height: 100%;
  margin: 0 auto;
}


</style>
