import { ref } from 'vue'
import { API_BASE, readErrorMessage } from './base.js'

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
            const res = await fetch(`${API_BASE}/accounts/${id}`)
            const data = await res.json().catch(() => ({}))
            if (!res.ok) throw new Error(readErrorMessage(data))
            account.value = data

        } catch (error) {
            console.error(error);
            //window.alert(error.message)

        }  
    };

    async function getOrder(id) {
        try {
            const res = await fetch(`${API_BASE}/orders/order/${id}`)
            const data = await res.json().catch(() => ({}))
            if (!res.ok) throw new Error(readErrorMessage(data))
            order.value = data

        } catch (error) {
            console.error(error);
           // window.alert(error.message)

        }  
    };

    async function getEmployees(userId) {
        try {
            const params = new URLSearchParams({ userID: String(userId) })
            const res = await fetch(`${API_BASE}/employees?${params}`)
            const data = await res.json().catch(() => ({}))
            if (!res.ok) throw new Error(readErrorMessage(data))
            employees.value = data
        } catch (error) {
            console.error(error);
           // window.alert(error.message)
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
            const params = new URLSearchParams({
                orderState: String(newState),
                accountId: String(accountId),
            })
            const res = await fetch(`${API_BASE}/orders/order/${orderId}/status?${params}`, {
                method: 'PUT',
            })
            const data = await res.json().catch(() => ({}))
            if (!res.ok) throw new Error(readErrorMessage(data))
            order.value = data
        } catch (error) {
            console.error(error);
            window.alert(error.message)
        }
    }; 
    
    async function assignEmployee(orderId, employeeId, accountId) {
        try {
            const params = new URLSearchParams({
                employeeId: String(employeeId),
                accountId: String(accountId),
            })
            const res = await fetch(`${API_BASE}/orders/order/${orderId}/assign-employee?${params}`, {
                method: 'PUT',
            })
            const data = await res.json().catch(() => ({}))
            if (!res.ok) throw new Error(readErrorMessage(data))
            order.value = data
        } catch (error) {
            console.error(error);
            window.alert(error.message)
        }
    };

    async function init(accountId, orderId) {
        loading.value = true; //set loading to true while fetching data
        await Promise.all([getAccount(accountId), getOrder(orderId)]); //wait for both account and order info to be fetched before proceeding

        if (account.value?.accountType === 'MANAGER' || account.value?.accountType === 'EMPLOYEE') {
            await getEmployees(accountId);
        }


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
