import { ref, computed} from 'vue';
import axios from 'axios'; //library for HTTP requests


// export function to be accessible from other files
// async function so that it can wait for the response from the backend before proceeding
export function useOrderInfo() {
    // Declare variables
    // ref tells variable is reactive. When the value changes, UI automatically updates.
    const order = ref(null); // this starts as nothing
    const account = ref(null);
    const employees = ref(null);
    const loading = ref(true);


    async function getAccount(id) {
        try {
            const response = await axios.get(`http://localhost:8080/account/${id}`); //get account info from backend using account ID
            account.value = response.data; //store account info in variable

        } catch (error) {
            console.error(error);
            window.alert(error.message)

        }  
    };

    async function getOrder(id) {
        try {
            const response = await axios.get(`http://localhost:8080/order/${id}`); //get order info from backend using order ID
            order.value = response.data; //store order info in variable

        } catch (error) {
            console.error(error);
            window.alert(error.message)

        }  
    };

    async function getEmployees() {
        try {
            const response = await axios.get(`http://localhost:8080/employee`); //get employees
            employees.value = response.data; //store employees in variable
        } catch (error) {
            console.error(error);
            window.alert(error.message)
        }
    };

    function canCancelOrder() {
        if (!order.value?.delivDate) return false;

        const currentTime = new Date();
        const delivery = new Date(order.value.delivDate);
        const difference = (delivery - currentTime) / (1000 * 60 * 60);

        return difference > 24;
    };

    async function updateOrderStatus(orderId, newState, accountId) {
        try {
            const response = await axios.put(`http://localhost:8080/order/${orderId}/status`, null, {
                params: { orderState: newState, accountId: accountId }
            });
            order.value = response.data; // Update local state with new data
        } catch (error) {
            console.error(error);
            window.alert(error.message)
        }
    }; 
    
    async function assignEmployee(orderId, employeeId, accountId) {
        try {
            const response = await axios.put(`http://localhost:8080/order/${orderId}/assign-employee`, null, {
                params: { employeeId: employeeId, accountId: accountId }
            });
            order.value = response.data;
        } catch (error) {
            console.error(error);
            window.alert(error.message)
        }
    };

    async function init(accountId, orderId) {
        loading.value = true; //set loading to true while fetching data
        await Promise.all([getAccount(accountId), getOrder(orderId), getEmployees()]); //wait for both account and order info to be fetched before proceeding
        loading.value = false; //set loading to false after data is fetched
    }

    return {
        order,
        account,
        employees,
        loading,
        canCancelOrder,
        init,
        updateOrderStatus,
        assignEmployee
    }
};

