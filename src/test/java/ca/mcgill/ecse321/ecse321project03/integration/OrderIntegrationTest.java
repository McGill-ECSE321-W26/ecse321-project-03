package ca.mcgill.ecse321.ecse321project03.integration;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderIntegrationTest {


    @Autowired
    private TestRestTemplate client;


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private Account testCustomer;
    private Account testAccountEmployee;
    private Employee testEmployee;
    private Account testAccountManager;
    private Manager testManager;

    private Item testItem;
    private ItemType testItemType1;
    private ItemType testItemType2;
    private Cart testCart;

    private CartItem testCartItem1;
    private CartItem testCartItem2;


    @BeforeEach
    @AfterEach
    public void clearDatabaseSetup() {
        orderRepository.deleteAll();
        orderItemRepository.deleteAll();
        accountRepository.deleteAll();
        employeeRepository.deleteAll();
        roleRepository.deleteAll();
        itemTypeRepository.deleteAll();
        cartItemRepository.deleteAll();
        managerRepository.deleteAll();
        itemRepository.deleteAll();
        cartRepository.deleteAll();
    }

    @BeforeEach
    public void setup(){

        // Customer
        testCustomer = new Account("Jeane", "jeane@gmail.com", "$fjdgbfdju1234", "1000 McGill", 0);
        accountRepository.save(testCustomer);

        testAccountEmployee = new Account("Gabriel", "gabriel@gmail.com", "$fjdgb23525", "2000 McGill", 0);
        accountRepository.save(testAccountEmployee);


        testAccountManager = new Account("Joseph", "joseph@gmail.com", "$fjdgb222/115", "3000 McGill", 0);
        accountRepository.save(testAccountManager);

        // ItemType
        testItem = new Item("beautiful", Item.Tag.Dress, 20);
        itemRepository.save(testItem);
        testItemType1 = new ItemType(ItemType.Size.S, ItemType.Color.Black, 10, testItem);
        itemTypeRepository.save(testItemType1);
        testItemType2 = new ItemType(ItemType.Size.M, ItemType.Color.Black, 7, testItem);
        itemTypeRepository.save(testItemType2);

        // Cart item
        testCart = new Cart(testCustomer);
        cartRepository.save(testCart);
        testCartItem1 = new CartItem(new CartItem.CartItemId(testItemType1, testCart), 2);
        cartItemRepository.save(testCartItem1);
        testCartItem2 = new CartItem(new CartItem.CartItemId(testItemType2, testCart), 3);
        cartItemRepository.save(testCartItem2);

        // Employee
        testEmployee = new Employee(testAccountEmployee);
        employeeRepository.save(testEmployee);

        // Manager
        testManager = new Manager(testAccountManager);
        managerRepository.save(testManager);

    }


    // ORDER CREATION AND CHECKOUT

    /**
     * Method that validates that creating a valid order correctly creates the object and checkout the cart items
     * @author Wissam Abchiche
     */
    @Test
    public void testValidCreateAndCheckoutOrder() {

        // Valid date
        Date orderDate = new Date(System.currentTimeMillis());
        Date delivDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);

        // Create the order
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), delivDate);

        // Act
        ResponseEntity<OrderResponseDto> response = client.postForEntity("/checkout", request, OrderResponseDto.class);

        // Assert the HTTP status
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        OrderResponseDto createdOrder =  response.getBody();


        assertNotNull(createdOrder);
        // Verify the correct data is returned
        assertEquals(delivDate, createdOrder.getDelivDate());
        assertEquals(orderDate, createdOrder.getOrderDate());
        assertEquals(Order.OrderState.Ordered.toString(), createdOrder.getOrderState());
        assertEquals(testCustomer.getId(), createdOrder.getCustomerId());

        // Ensure the generated id is positive
        assertTrue(createdOrder.getId() > 0, "Response should have a positive ID.");


        // Verify that the cart items list is now empty
        Cart createdCart = cartRepository.findByAccount_Id(createdOrder.getCustomerId());
        assertNotNull(createdCart);
        List<CartItem> createdCartItems = cartItemRepository.findByCartItemId_Cart(createdCart);
        assertNotNull(createdCartItems);
        assertTrue(createdCartItems.isEmpty());

        // Verify it updates the stock of all itemTypes that were in the cart
        ItemType createdItemType1 = itemTypeRepository.findById(testItemType1.getId()).orElse(null);
        assertEquals(8, createdItemType1.getStock());
        ItemType createdItemType2 = itemTypeRepository.findById(testItemType2.getId()).orElse(null);
        assertEquals(4, createdItemType2.getStock());
    }


    /**
     * Method which verifies that the invalid action corresponds to a certain HttpStatus and an expectedError
     * @author Wissam Abchiche
     * @param request the OrderCreationDto to send in the request body
     * @param expectedHttpStatus    the expected HTTP status code of the response
     * @param expectedErrorMessage  the expected error message in the response body
     */
    private void invalidTestForCreateOrderAndCheckout(OrderCreationDto request, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
        // Verify the status code is the expected one
        ResponseEntity<ErrorDto> response = client.postForEntity("/checkout", request, ErrorDto.class);
        assertEquals(expectedHttpStatus, response.getStatusCode());

        // Verify that the error message returned is what is expected
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedErrorMessage, responseBody.getErrors().get(0));
    }

    /**
     * Method that invalidates the creation of an order when given an invalid id
     * @author Wissam Abchiche
     */
    @Test
    public void testCreateOrderAndCheckoutInvalidCustomerId() {
        // Create order with invalid customer id
        Date deliveryDate = new Date(System.currentTimeMillis() + 30 * 60 * 60 * 1000);
        OrderCreationDto request = new OrderCreationDto(-1, deliveryDate);

        invalidTestForCreateOrderAndCheckout(request, HttpStatus.NOT_FOUND, "No customer found with id -1" );
    }

    /**
     * Method that invalidates the creation of an order when given null delivery date
     * @author Wissam Abchiche
     */
    @Test
    public void testCreateOrderAndCheckoutNullDelivDate() {
        // Create order with null delivery date
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), null);

        invalidTestForCreateOrderAndCheckout(request, HttpStatus.NOT_FOUND, "No delivery date found" );

    }


    /**
     * Method that invalidates the creation of an order when given an invalid delivery date (less than 24 hours from order date)
     * @author Wissam Abchiche
     */
    @Test
    public void testCreateOrderAndCheckoutInvalidDelivDate() {
        // Create order with a delivery date representing the current date, which is also the current order date
        Date currentDate = new Date(System.currentTimeMillis());
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), currentDate);

        invalidTestForCreateOrderAndCheckout(request, HttpStatus.BAD_REQUEST, "The delivery date can not be placed less than 24 hours after the order date");
    }

    /**
     * Method that invalidates the checkout of an order when given an empty cart
     * @author Wissam Abchiche
     */
    @Test
    public void testCreateOrderAndCheckoutInvalidCart() {
        // Create a valid order
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), deliveryDate);


        // Delete all the cart items of cart
        Cart cart = cartRepository.findByAccount_Id(testCustomer.getId());
        cartItemRepository.deleteAll(cartItemRepository.findByCartItemId_Cart(cart));

        invalidTestForCreateOrderAndCheckout(request, HttpStatus.BAD_REQUEST, "Cannot checkout with an empty cart");

    }

    /**
     * Method that invalidates the checkout of an order when given an invalid quantity of cart items
     * @author Wissam Abchiche
     */
    @Test
    public void testCreateOrderAndCheckoutInvalidQuantity() {
        // Create an invalid cartItem quantity (less or equal to 0) and save in the repo
        testCartItem1.setQuantity(0);
        cartItemRepository.save(testCartItem1);

        // Create a valid order
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), deliveryDate);

        invalidTestForCreateOrderAndCheckout(request, HttpStatus.BAD_REQUEST, "Cart contains an invalid quantity: 0");

    }

    /**
     * Method that invalidates the checkout of an order when given a quantity of cartItems in the cart higher than the stock
     * @author Wissam Abchiche
     */
    @Test
    public void testCreateOrderAndCheckoutNotEnoughStock() {
        // Create an invalid cartItem quantity due to not enough stock and save in the repo
        testCartItem1.setQuantity(testItemType1.getStock() + 5); // too many
        cartItemRepository.save(testCartItem1);

        // Create a valid order
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), deliveryDate);

        invalidTestForCreateOrderAndCheckout(request, HttpStatus.BAD_REQUEST, "Not enough stock for itemType id " + testItemType1.getId() +
                 ". Requested " + (testItemType1.getStock() + 5) + ", available " + (testItemType1.getStock()));

    }

    // FIND BY ID

    /**
     * Method which verifies that the order was found when given a valid id
     * @author Wissam Abchiche
     */
    @Test
    public void testValidFindOrderById() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        // Verify that the status code is OK
        ResponseEntity<OrderResponseDto> response = client.getForEntity("/orders/" + order.getId(), OrderResponseDto.class);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify the correct data is returned
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(responseBody.getDelivDate(), order.getDelivDate());
        assertEquals(responseBody.getOrderDate(), order.getOrderDate());
        assertEquals(responseBody.getOrderState(), order.getOrderState().toString());
        assertEquals(responseBody.getId(), order.getId());
        assertEquals(responseBody.getCustomerId(), order.getCustomer().getId());
        if (order.getAssignee() == null) {
            assertEquals(0, responseBody.getAssigneeId());
        } else {
            assertEquals(responseBody.getAssigneeId(), order.getAssignee().getId());
        }
    }


    // ASSIGN EMPLOYEE TO ORDER

    /**
     * Method which verifies that the invalid action corresponds to a certain HttpStatus and an expectedError
     * @author Wissam Abchiche
     * @param url the url to verify
     * @param expectedHttpStatus    the expected HTTP status code of the response
     * @param expectedErrorMessage  the expected error message in the response body
     */
    private void invalidTestForAssignEmployee(String url, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
        // Verify the status code is the expected one
        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);
        assertNotNull(response);
        assertEquals(expectedHttpStatus, response.getStatusCode());

        // Verify that the error message returned is what is expected
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedErrorMessage, responseBody.getErrors().get(0));
    }

    /**
     * Method which returns the url of assignEmployee
     * @author Wissam Abchiche
     */
    private String assignEmployeeUrl(int orderId, int employeeId, int accountId){
        return "/orders/" + orderId + "/assign-employee"
                + "?employeeId=" + employeeId
                + "&accountId=" + accountId;
    }

    /**
     * Method which verifies that the employee was successfully assigned
     * @author Wissam Abchiche
     */
    private void testValidAssignEmployee(int accountId) {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);


        ResponseEntity<OrderResponseDto> response = client.exchange(
                assignEmployeeUrl(order.getId(), testEmployee.getId(), accountId),
                HttpMethod.PUT, null, OrderResponseDto.class);


        // Verify the status code is Http.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify the assignment was successful
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testEmployee.getId(), responseBody.getAssigneeId());
    }

    /**
     * Method which verifies that the employee was successfully assigned by a manager to an order
     * @author Wissam Abchiche
     */
    @Test
    public void testValidAssignEmployeeByManager() {
        testValidAssignEmployee(testManager.getAccount().getId());
    }

    /**
     * Method which verifies that the employee successfully assigned himself
     * @author Wissam Abchiche
     */
    @Test
    public void testValidAssignEmployeeByEmployee() {
        testValidAssignEmployee(testEmployee.getAccount().getId());

    }

    /**
     * Method which verifies that the employee is not assigned when passing an invalid order
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignEmployeeOrderNull() {
        invalidTestForAssignEmployee(assignEmployeeUrl(-1, testEmployee.getId(), testEmployee.getAccount().getId()),
                HttpStatus.NOT_FOUND, "No order found with id -1");
    }

    /**
     * Method which verifies that the employee is not assigned when passing an invalid account
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignEmployeeAccountNull() {

        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestForAssignEmployee(assignEmployeeUrl(order.getId(), testEmployee.getId(), -1),
                HttpStatus.NOT_FOUND, "No account doing the action found with id -1");
    }

    /**
     * Method which verifies that the employee is not assigned when customer is making the change
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignEmployeeCustomerMakingTheChange() {

        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);


        invalidTestForAssignEmployee(assignEmployeeUrl(order.getId(), testEmployee.getId(), testCustomer.getId()),
                HttpStatus.FORBIDDEN, "The order cannot be assigned to the employee with id " + testEmployee.getId() + " as a customer is making the change.");
    }


    /**
     * Method which verifies that the employee is not assigned when he doesn't exist
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignEmployeeEmployeeNotFound() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestForAssignEmployee(assignEmployeeUrl(order.getId(), -1, testManager.getAccount().getId()),
                HttpStatus.NOT_FOUND, "No employee found with id -1");
    }

    /**
     * Method which verifies that the employee is not assigned when he assigns himself to the order he ordered
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignEmployeeEmployeeIsCustomer() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        // Create an employee attached to the customer
        Employee employeeIsCustomer = new Employee(testCustomer);
        employeeRepository.save(employeeIsCustomer);


        invalidTestForAssignEmployee(assignEmployeeUrl(order.getId(), employeeIsCustomer.getId(), testManager.getAccount().getId()),
                HttpStatus.FORBIDDEN, "The order cannot be assigned to the employee with id " + employeeIsCustomer.getId() + " as he is the customer.");

    }

    /**
     * Method which verifies that the employee is not assigned when he assigns someone else than himself
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignEmployeeByAnotherEmployee() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        // Create a new employee for test
        Account accountNewEmployee = new Account("Dora", "dora@gmail.com", "ewffce3f%%0", "4000 McGill", 0);
        accountRepository.save(accountNewEmployee);
        Employee newEmployee = new Employee(accountNewEmployee);
        employeeRepository.save(newEmployee);


        invalidTestForAssignEmployee(assignEmployeeUrl(order.getId(), newEmployee.getId(), testEmployee.getAccount().getId()),
                HttpStatus.FORBIDDEN, "The order cannot be assigned to the employee by another employee.");
    }




    // UPDATE ORDER STATUS

    /**
     * Method which verifies that the invalid action corresponds to a certain HttpStatus and an expectedError
     * @author Wissam Abchiche
     * @param url the url to verify
     * @param expectedHttpStatus    the expected HTTP status code of the response
     * @param expectedErrorMessage  the expected error message in the response body
     */
    private void invalidTestUpdateStatus(String url, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
        // Verify the status code is the expected one
        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);
        assertNotNull(response);
        assertEquals(expectedHttpStatus, response.getStatusCode());

        // Verify that the error message returned is what is expected
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedErrorMessage, responseBody.getErrors().get(0));
    }

    /**
     * Method which returns the url of updateStatus
     * @author Wissam Abchiche
     */
    private String updateStatusUrl(int orderId, String status, int accountId){
        return "/orders/" + orderId + "/status"
                + "?orderState=" + status
                + "&accountId=" + accountId;
    }


    /**
     * Method which verifies that the valid assignment updates the order state
     * @author Wissam Abchiche
     * @param orderState current state of the order
     * @param updatedOrderState expected new order state
     */
    private void testValidUpdateStatus(Order.OrderState orderState, String updatedOrderState) {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, orderState, testCustomer,null);
        orderRepository.save(order);

        ResponseEntity<OrderResponseDto> response = client.exchange(
                updateStatusUrl(order.getId(), updatedOrderState, testEmployee.getAccount().getId()),
                HttpMethod.PUT, null, OrderResponseDto.class);

        // Verify the status code is HTTP.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify the assignment was successful
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(updatedOrderState, responseBody.getOrderState());
    }

    /**
     * Method which verifies the valid update from Ordered to InPreparation
     * @author Wissam Abchiche
     */
    @Test
    public void testValidUpdateStatusOrderedToInPreparation() {
        testValidUpdateStatus(Order.OrderState.Ordered, "InPreparation");
    }

    /**
     * Method which verifies the valid update from InPreparation to Prepared
     * @author Wissam Abchiche
     */
    @Test
    public void testValidUpdateStatusInPreparationToPrepared() {
        testValidUpdateStatus(Order.OrderState.InPreparation, "Prepared");
    }


    /**
     * Method which verifies the valid update from Prepared to Delivered
     * @author Wissam Abchiche
     */
    @Test
    public void testValidUpdateStatusPreparedToDelivered() {
        testValidUpdateStatus(Order.OrderState.Prepared, "Delivered");
    }

    /**
     * Method which verifies the valid update to Cancelled (and the cartItems are removed and put back to stock)
     * @author Wissam Abchiche
     */
    @Test
    public void testValidUpdateStatusToCancelled() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        ResponseEntity<OrderResponseDto> response = client.exchange(
                updateStatusUrl(order.getId(), "Cancelled", testEmployee.getAccount().getId()),
                HttpMethod.PUT, null, OrderResponseDto.class);

        // Verify the status code is HTTP.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify the assignment was successful
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Cancelled", responseBody.getOrderState());

        // Verify it updates the stock of all itemTypes that were in the cart
        ItemType itemType1 = itemTypeRepository.findById(testItemType1.getId()).orElse(null);
        assertEquals(10, itemType1.getStock());
        ItemType itemType2 = itemTypeRepository.findById(testItemType2.getId()).orElse(null);
        assertEquals(7, itemType2.getStock());

        // Verify cart is empty
        Cart cart = cartRepository.findByAccount_Id(order.getCustomer().getId());
        assertNotNull(order);
        List<CartItem> cartItems = cartItemRepository.findByCartItemId_Cart(cart);
        assertNotNull(cartItems);
        assertTrue(cartItems.isEmpty());
    }


    /**
     * Method which invalid the update when order doesn't exist
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusForOrderThatDoesNotExist() {

        invalidTestUpdateStatus(updateStatusUrl(-1, "Delivered", testManager.getAccount().getId()),
                HttpStatus.NOT_FOUND, "No order found with id -1");
    }

    /**
     * Method which invalidates the update when account doesn't exist
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusToCancellationByAccountThatDoesNotExist() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestUpdateStatus(updateStatusUrl(order.getId(), "Cancelled", -1),
                HttpStatus.NOT_FOUND, "No account found with id -1");
    }

    /**
     * Method which invalidates the cancellation when less than 24 hours from delivery date
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusToCancellationLess24HoursToDelivery() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 12 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);
        invalidTestUpdateStatus(updateStatusUrl(order.getId(), "Cancelled", testManager.getAccount().getId()),
                HttpStatus.BAD_REQUEST, "The order cannot be cancelled less than 24 hours before the delivery date");

    }

    /**
     * Method which invalidates the cancellation by an employee
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusToCancellationByEmployee() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestUpdateStatus(updateStatusUrl(order.getId(), "Cancelled", testEmployee.getAccount().getId()),
                HttpStatus.FORBIDDEN, "The order cannot be cancelled by an employee");

    }


    /**
     * Method which invalidates the update by a customer
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusByCustomer() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestUpdateStatus(updateStatusUrl(order.getId(), "InPreparation", testCustomer.getId()),
                HttpStatus.FORBIDDEN, "The order state cannot be updated by a customer");
    }

    /**
     * Method which invalidates the update from Ordered to Prepared
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusFromOrderedToPrepared() {

        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestUpdateStatus(updateStatusUrl(order.getId(), "Prepared", testManager.getAccount().getId()),
                HttpStatus.BAD_REQUEST, "Order with id " + order.getId() + " cannot be switched to Prepared because it is not InPreparation");

    }

    /**
     * Method which invalidates the update from Ordered to Delivered
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusFromOrderedToDelivered() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestUpdateStatus(updateStatusUrl(order.getId(), "Delivered", testManager.getAccount().getId()),
                HttpStatus.BAD_REQUEST, "Order with id " + order.getId() + " cannot be switched to Delivered because it is not Prepared");

    }

    /**
     * Method which invalidates the update from InPreparation to Delivered
     * @author Wissam Abchiche
     */
    @Test
    public void testUpdateStatusFromInPreparationToDelivered() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.InPreparation, testCustomer,null);
        orderRepository.save(order);

        invalidTestUpdateStatus(updateStatusUrl(order.getId(), "Delivered", testManager.getAccount().getId()),
                HttpStatus.BAD_REQUEST, "Order with id " + order.getId() + " cannot be switched to Delivered because it is not Prepared");

    }

    // VIEW ORDERS

    /**
     * Method which validates the view of orders
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewOrders() {
        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);


        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/orders?userID=" + testManager.getAccount().getId(), OrderResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(1, responseBody.length);
        assertEquals(order.getId(), responseBody[0].getId());
    }


    /**
     * Method which invalidates the view of orders if done by customer
     * @author Wissam Abchiche
     */
    @Test
    public void testInvalidViewOrders() {

        ResponseEntity<ErrorDto> response = client.getForEntity(
                "/orders?userID=" + testCustomer.getId(), ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Customers may not view of all of the orders in the system", responseBody.getErrors().get(0));
    }



    /**
     * Method which verifies that the invalid action corresponds to a certain HttpStatus and an expectedError
     * @author Wissam Abchiche
     * @param url the url to verify
     * @param expectedHttpStatus    the expected HTTP status code of the response
     * @param expectedErrorMessage  the expected error message in the response body
     */
    private void invalidViewOrders(String url, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
        // Verify the status code is the expected one
        ResponseEntity<ErrorDto> response = client.getForEntity(
                url, ErrorDto.class);
        assertNotNull(response);
        assertEquals(expectedHttpStatus, response.getStatusCode());

        // Verify that the error message returned is what is expected
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedErrorMessage, responseBody.getErrors().get(0));

    }



    /**
     * Method which verifies that the valid view of orders by date ascending
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewOrdersByDateAscending() {

        // Create valid orders and save them in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);
        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/orders-date?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "ASC", OrderResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(order.getId(), responseBody[0].getId());
        assertEquals(order2.getId(), responseBody[1].getId());
    }



    /**
     * Method which verifies that the valid view of orders by date descending
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewOrdersByDateDescending() {

        // Create valid orders and save them in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);
        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/orders-date?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(order.getId(), responseBody[1].getId());
        assertEquals(order2.getId(), responseBody[0].getId());
    }


    /**
     * Method which invalidates the view of orders by date descending by customer
     * @author Wissam Abchiche
     */
    @Test
    public void testInvalidViewOrdersByDate() {
        invalidViewOrders("/orders-date?userID=" + testCustomer.getId()  +"&filterDirection=" + "descending", HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");
    }

    /**
     * Method which verifies that the valid view of orders by customer descending
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewOrdersByCustomerDescending() {

        // Create second customer
        Account customer2 = new Account("albert", "albert@gmail.com", "revfgnr33e!10", "6000 McGill", 0);
        accountRepository.save(customer2);

        // Create valid orders and save them in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);
        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, customer2, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/orders-customer?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(order.getId(), responseBody[1].getId());
        assertEquals(order2.getId(), responseBody[0].getId());
    }



    /**
     * Method which verifies that the valid view of orders by customer ascending
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewOrdersByCustomerAscending() {

        // Create second customer
        Account customer2 = new Account("albert", "albert@gmail.com", "revfgnr33e!10", "6000 McGill", 0);
        accountRepository.save(customer2);

        // Create valid orders and save them in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);
        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, customer2, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/orders-customer?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "asc", OrderResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(order.getId(), responseBody[0].getId());
        assertEquals(order2.getId(), responseBody[1].getId());
    }


    /**
     * Method which invalidates the view of orders by customer descending by customer
     * @author Wissam Abchiche
     */
    @Test
    public void testInvalidViewOrdersByCustomer() {

        invalidViewOrders("/orders-customer?userID=" + testCustomer.getId()  +"&filterDirection=" + "descending", HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");

    }

    /**
     * Method which verifies that the valid view of orders by order state descending
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewOrdersByOrderStateDescending() {

        // Create valid orders and save them in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);
        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Delivered, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/orders-status?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(order.getId(), responseBody[1].getId());
        assertEquals(order2.getId(), responseBody[0].getId());
    }


    /**
     * Method which verifies that the valid view of orders by order state ascending
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewOrdersByOrderStateAsc() {

        // Create valid orders and save them in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);
        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Delivered, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/orders-status?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "asc", OrderResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(order.getId(), responseBody[0].getId());
        assertEquals(order2.getId(), responseBody[1].getId());
    }

    /**
     * Method which invalidates the view of orders by status descending by customer
     * @author Wissam Abchiche
     */
    @Test
    public void testInvalidViewOrdersByStatus() {
        invalidViewOrders("/orders-status?userID=" + testCustomer.getId()  +"&filterDirection=" + "descending", HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");


    }

}
