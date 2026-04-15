import { ref } from 'vue'
import { API_BASE, readErrorMessage } from './base.js'

// export function to be accessible from other files
// async function so that it can wait for the response from the backend before proceeding

/**
 * Create all necessary functions to manage order-related state and actions.
 *
 * @returns @returns {{
 *  order: order information of the order to manage
 *  account: account information associated to the order
 *  employees: list of employees in the system
 *  loading: reactive state variable
 *  canCancelOrder: function to check who is allowed to cancel an order (UI depends on that)
 *  updateOrderStatus: function that updates the order status
 *  assignEmployee: function that assign an employee (from controller)
 *  init: main setup of the composable
 *  }}
 * @author Wissam Abchiche
 */
export function useOrderInfo() {
    // Declare variables
    // ref tells variable is reactive. When the value changes, UI automatically updates.
    const order = ref(null); // this starts as nothing
    const account = ref(null);
    const employees = ref(null);
    const loading = ref(true);

    /**
     * Fetches account information by ID.
     *
     * @param {number} id - Account ID
     * @returns {Promise<void>}
     * @author Wissam Abchiche
     */
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

    /**
     * Fetches order information by ID.
     *
     * @param {number} id - Order ID
     * @returns {Promise<void>}
     * @author Wissam Abchiche
     */
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

    /**
     * Fetches employees
     *
     * @param {number} userId - User ID of the user performing the action
     * (only manager and employee can view all employees)
     * @returns {Promise<void>}
     * @author Wissam Abchiche
    */
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

    /**
     * Determines whether the current order can be cancelled.
     * An order can be cancelled only if delivery is more than 24 hours away.
     *
     * @returns {boolean}
     * @author Wissam Abchiche
     */
    function canCancelOrder() {
        if (!order.value?.delivDate) return false;

        const currentTime = new Date();
        const delivery = new Date(order.value.delivDate);
        const difference = (delivery - currentTime) / (1000 * 60 * 60);

        return difference > 24;
    };

    /**
     * Updates the status of an order.
     *
     * @param {number} orderId - Order ID
     * @param {string} newState - New order state
     * @param {number} accountId - Account ID performing the action
     * @returns {Promise<void>}
     * @author Wissam Abchiche
     */
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

    /**
     * Assigns an employee to an order.
     *
     * @param {number} orderId - Order ID
     * @param {number} employeeId - Employee ID to assign
     * @param {number} accountId - Account performing the action
     * @returns {Promise<void>}
     * @author Wissam Abchiche
     */
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

    /**
     * Initializes the composable
     *
     * @param {number} accountId - Account ID
     * @param {number} orderId - Order ID
     * @returns all functions defined above
     * @author Wissam Abchiche
     */
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
