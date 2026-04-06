<script setup>
import Header from '../views/Header.vue'; // Ensure this path matches where you saved the Header component
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useOrderInfo } from '../api/orderInfo.js'; 
import { getSession, isLoggedIn } from '../session/storage.js';

const route = useRoute();
const router = useRouter();
const { order, account, employees, loading, canCancelOrder, init, updateOrderStatus, assignEmployee } = useOrderInfo();
const selectedStatus = ref('');
const selectedEmployeeId = ref(null);
const isSelfAssignChecked = ref(false);

async function saveButtonAction() {
  const session = getSession();
  const orderId = order.value.orderId;
  const accountId = session.accountId;


  if (account.value.accountType === 'EMPLOYEE') {
    if (isSelfAssignChecked.value) {
      const employeeId = employees.value.find(e => e.account.id === accountId).employeeId;
      await assignEmployee(orderId, employeeId, accountId);
    }
    if (selectedStatus.value) {
      await updateOrderStatus(orderId, selectedStatus.value, accountId);
    }
  }

  if (account.value.accountType === 'CUSTOMER' && selectedStatus.value) {
    
    await updateOrderStatus(orderId, selectedStatus.value, accountId);
  }

  if (account.value.accountType === 'MANAGER') {
    if (selectedEmployeeId.value) {
      await assignEmployee(orderId, selectedEmployeeId.value, accountId);
    }
    if (selectedStatus.value) {
      await updateOrderStatus(orderId, selectedStatus.value, accountId);
    }
  }
};

onMounted(() => {
  if (!isLoggedIn()) {
    router.push('/login');
    return;
  }
  const orderId = route.params.id;
  const session = getSession();
  init(session.accountId, orderId);
});


// FOR TESTING
order.value = {
  orderId: "ORD-12345",
  items: [1, 2, 3],
  assigneeId: 1,
  customerId: "CUST-99",
  orderState: "Delivered",
  orderDate: "2026-05-01",
  delivDate: "2026-05-05"
};

account.value = { accountType: 'EMPLOYEE' }; // Matches your localStorage fake

employees.value = [
  { id: 101 },
  { id: 102 },
  { id: 103 }
];

// FOR TESTING
</script>

<template>
  <div class="page-layout">
    <Header activeTab="orders" />

    <div class="container">
      <div class="box__wrapper">
        <div class="box__background"></div>
        
        <div class="box">
          <div class="title__rectangle">
            Order Information
            <button class="buttonReturn" @click="$router.back()"></button> 
          </div> 

          <div class="main__content">
            <div class="order__summary__container">
              <div class="order__summary__text">
                  <strong>Order ID:</strong> 
                  <div>{{ order?.orderId ?? 0 }}</div>
              </div>
              <div class="order__summary__text">
                <strong>Total Items:</strong> 
                <div>{{ order?.items?.length ?? 0 }}</div>
              </div>
              <button class="buttonViewItems">View Items</button> 
            </div>

            <div class="info__container">
              <div class="text__info__rectangle">
                <span class="label">Employee ID:</span>
                <span class="underscores">_______</span>    
                <div v-if="order?.assigneeId !==0" class="box__ID">
                  {{ order?.assigneeId }}
                </div>

                
                <div v-else-if="account?.accountType === 'CUSTOMER'" class="box__ID">
                  None
                </div> 

                <div v-else-if="account?.accountType === 'EMPLOYEE'">                  
                  <label class="container">Self-assign 
                      <input type="checkbox" v-model="isSelfAssignChecked">
                      <span class="checkmark"></span>
                  </label>
                </div>
                
                <select v-else-if="account?.accountType === 'MANAGER'"
                  class="box__ID" v-model="selectedEmployeeId">
                  <option :value="null">None</option>
                  <option v-for="employee in employees" 
                      :key="employee.id" 
                      :value="employee.id">
                      {{ employee.id }}
                  </option>
                </select> 
              </div> 

              <div class="text__info__rectangle">
                <span class="label">Customer ID:</span>
                <span class="underscores">_______</span>  
                <div class="box__ID">
                  {{ order?.customerId  }}
                </div>
              </div>

              <div class="text__info__rectangle">
                <span class="label">Status:</span>
                <span class="underscores">______</span>  

                <template v-if="account?.accountType === 'CUSTOMER'">
                  <select v-if="order?.orderState !== 'Delivered' && canCancelOrder()" v-model="selectedStatus" class="box__date__state">
                    <option value="">{{ order?.orderState }} </option>
                    <option value="Cancelled">Cancelled</option>
                  </select>
                  <div v-else class="box__date__state">{{ order?.orderState }}</div>
                </template>

                <template v-else-if="account?.accountType === 'EMPLOYEE'">
                  <select v-if="order?.orderState !== 'Delivered'" v-model="selectedStatus" class="box__date__state">
                    <option value="">{{ order?.orderState }} </option>
                    <option v-if="order?.orderState === 'Ordered'" value="InPreparation">InPreparation</option>
                    <option v-if="order?.orderState === 'InPreparation'" value="Prepared">Prepared</option>
                    <option v-if="order?.orderState === 'Prepared'" value="Delivered">Delivered</option>
                  </select>
                  <div v-else class="box__date__state">{{ order?.orderState }}</div>
                </template>

                <template v-else-if="account?.accountType === 'MANAGER'">
                  <select v-if="order?.orderState !== 'Delivered'" v-model="selectedStatus" class="box__date__state">
                    <option value="">{{ order?.orderState }} </option>
                    <option v-if="order?.orderState === 'Ordered'" value="InPreparation">InPreparation</option>
                    <option v-if="order?.orderState === 'Ordered'" value="Cancelled">Cancelled</option>
                    <option v-if="order?.orderState === 'InPreparation'" value="Prepared">Prepared</option>
                    <option v-if="order?.orderState === 'InPreparation'" value="Cancelled">Cancelled</option>
                    <option v-if="order?.orderState === 'Prepared'" value="Delivered">Delivered</option>
                    <option v-if="order?.orderState === 'Prepared'" value="Cancelled">Cancelled</option>
                  </select>
                  <div v-else class="box__date__state">{{ order?.orderState }}</div>
                </template>

                <div v-else class="box__date__state">{{ order?.orderState }}</div>
              </div>

              <div class="text__info__rectangle">
                <span class="label">Order Date:</span>
                <span class="underscores">__</span>
                <div class="box__date__state">
                  {{ order?.orderDate }}
                </div>
              </div>
              
              <div class="text__info__rectangle">
                <span class="label">Deliv. Date:</span>
                <span class="underscores">_</span>
                <div class="box__date__state">
                  {{ order?.delivDate }}
                </div>
              </div>  
            </div>
          </div>

          <div class="footer">
            <button 
              v-if="!(order?.orderState === 'Delivered' && order?.assigneeId !== 0) && (account?.accountType !== 'CUSTOMER' || canCancelOrder())" 
              class="buttonSave" 
              @click="saveButtonAction"
            >
              Save changes
            </button> 
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inconsolata:wght@400;700&display=swap');

/* NEW: Wraps header and content */
.page-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  background-color: #f7f4f1; /* Match AppShell background color */
}

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1; /* Takes up remaining height below header */
  width: 100%;
  font-family: 'Inconsolata';
  color : #000000;
}

.box__wrapper {
  position: relative;
  display: flex;
}

.box__background {
  width: 921px;
  height: 612px;
  border: 5px solid #3D3C3C;
  background-color: #3D3C3C; 
  position: absolute;
  top: 26px;
  left: 26px;
  z-index: 1;
}

.box {
  position: relative;
  z-index: 2;
  width: 921px;
  height: 612px;
  border: 5px solid #1209AB;
  background-color: #FFFFFF;
  display: flex;
  flex-direction: column;
}

.title__rectangle {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 78px;
  border-bottom: 5px solid #1209AB;
  background-color: #D8BFF1;
  font-size: 32px;
  font-weight: bold;
}

.buttonReturn {
  position: absolute;
  right: 5px;
  width: 61px;
  height: 61px;
  background: #FFFFFF; 
  border: 5px solid #3D3C3C;  
  background-image: url('../assets/images/return-button.png');
  background-size: 80%;      
  background-repeat: no-repeat;  
  background-position: center;
  cursor: pointer;
}

.main__content {
  flex: 1;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;
}

.order__summary__container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 312px;
  height: 359px;
  background: #F7F7F7;
  border-radius: 31px;
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.25);
  gap: 20px;
}

.order__summary__text {
  font-size: 29px;
  text-align: center;
}

.info__container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 450px;
}

.text__info__rectangle {
  display: flex;
  align-items: center;
  font-size: 29px;
 }

.box__ID {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 160px;
  height: 41px;
  padding: 5px 20px;
  background: #F7F7F7;
  border-radius: 20px;
  box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  font-size: 24px;
  font-family: 'Inconsolata';
}

.box__date__state {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  text-align-last: center;
  width: 252px;
  height: 41px;
  padding: 5px 20px;
  background: #F7F7F7;
  border-radius: 20px;
  box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  font-size: 24px;
  font-family: 'Inconsolata', monospace;
}

.footer {
  display: flex;
  justify-content: center;
  padding-bottom: 30px;
}

.buttonSave, .buttonViewItems {
  background: linear-gradient(to top, #FFFFFF 0%, #FFEEFB 30%, #FDCEE9 80%);
  border-radius: 20px;
  width: 231px;
  height: 60px;
  border: 1px solid #000000;  
  font-size: 29px;
  cursor: pointer;
  font-family: 'Inconsolata';
}


.underscores {
  color: #FFFFFF; 
}
</style>