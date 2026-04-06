<script setup>

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

  // 1. Logic for EMPLOYEE (Check self-assignment and status update)
  if (account.value.accountType === 'EMPLOYEE') {
    if (isSelfAssignChecked.value) {
      const employeeId = employees.value.find(e => e.account.id === accountId).employeeId;
      await assignEmployee(orderId, employeeId, accountId);
    }
    if (selectedStatus.value) {
      await updateOrderStatus(orderId, selectedStatus.value, accountId);
    }
  }

  // 2. Logic for CUSTOMER (Check status/cancellation)
  if (account.value.accountType === 'CUSTOMER' && selectedStatus.value) {
    await updateOrderStatus(orderId, selectedStatus.value, accountId);
  }

  // 3. Logic for MANAGER (Check employee assignment and status)
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
  //1. Check if the user is logged in
  if (!isLoggedIn()) {
    router.push('/login'); // Redirect to login page if not logged in
    return;
  }

  //2. get the ids
  const orderId = route.params.id; // id comes from router
  const session = getSession(); // id comes from local storage

  // 3. Initialize the data fetch
  init(session.accountId, orderId);
});


</script>



<template>
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

              <div v-else-if="account?.accountType === 'CUSTOMER'" class="box__employeeID">
                None
              </div> 

              <input v-else-if="account?.accountType === 'EMPLOYEE'" 
              type="checkbox" class="checkBox" v-model="isSelfAssignChecked"/> 
              

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
                <select v-model="selectedStatus" class="box__date__state">
                  <option :value="order?.orderState">{{ order?.orderState }} </option>
                  <option v-if="canCancelOrder()" value="Cancelled">Cancel</option>
                </select>
              </template>
              
              <template v-else-if="account?.accountType === 'EMPLOYEE'">
                <select v-model="selectedStatus" class="box__date__state">
                  <option value="">{{ order?.orderState }} </option>
                  <option v-if="order?.orderState === 'Ordered'" value="Ordered">Ordered</option>
                  <option v-if="order?.orderState === 'Ordered'" value="InPreparation">InPreparation</option>

                  <option v-if="order?.orderState === 'InPreparation'" value="InPreparation">InPreparation</option>
                  <option v-if="order?.orderState === 'InPreparation'" value="Prepared">Prepared</option>

                  <option v-if="order?.orderState === 'Prepared'" value="Prepared">Prepared</option>
                  <option v-if="order?.orderState === 'Prepared'" value="Delivered">Delivered</option>
                </select>
              </template>

              <template v-else-if="account?.accountType === 'MANAGER'">
                <select v-model="selectedStatus" class="box__date__state">
                  <option value="">{{ order?.orderState }} </option>
                  <option v-if="order?.orderState === 'Ordered'" value="Ordered">Ordered</option>
                  <option v-if="order?.orderState === 'Ordered'" value="InPreparation">InPreparation</option>
                  <option v-if="order?.orderState === 'Ordered'" value="Cancelled">Cancelled</option>

                  <option v-if="order?.orderState === 'InPreparation'" value="InPreparation">InPreparation</option>
                  <option v-if="order?.orderState === 'InPreparation'" value="Prepared">Prepared</option>
                  <option v-if="order?.orderState === 'InPreparation'" value="Cancelled">Cancelled</option>

                  <option v-if="order?.orderState === 'Prepared'" value="Prepared">Prepared</option>
                  <option v-if="order?.orderState === 'Prepared'" value="Delivered">Delivered</option>
                  <option v-if="order?.orderState === 'Prepared'" value="Cancelled">Cancelled</option>
                </select>
              </template>

              <template v-else>
                <div class="box__date__state">{{ order?.orderState }}</div>
              </template>
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
          <button class="buttonSave" @click = "saveButtonAction">Save changes</button> 
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inconsolata:wght@400;700&display=swap');

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  background-color: #FFFFFF;
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
}

.box__date__state {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 252px;
  height: 41px;
  padding: 5px 20px;
  background: #F7F7F7;
  border-radius: 20px;
  box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.15);
  min-width: 120px;
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

.checkBox {
  width: 30px;
  height: 30px;
  border: 2px solid #3D3C3C;
  background-color: #FFFFFF;
  cursor: pointer;
  margin-left: 120px;
}

.underscores {
  color: #FFFFFF; 
}

</style>