package ca.mcgill.ecse321.ecse321project03.integration;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.repository.*;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
import  org.junit.jupiter.api.*;


import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


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

    private int validOrderId;


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
        cartItemRepository.deleteAll();
        managerRepository.deleteAll();
        itemRepository.deleteAll();
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




    @Test
    @org.junit.jupiter.api.Order(0)
    public void testValidCreateAndCheckoutOrder() {

        // Valid date
        Date orderDate = new Date(System.currentTimeMillis());
        Date delivDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);

        // Arrange
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), delivDate);

        // Act
        ResponseEntity<OrderResponseDto> response = client.postForEntity("/checkout", request, OrderResponseDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
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
        assertEquals(4, createdItemType1.getStock());

        this.validOrderId = createdOrder.getId();

    }

    @Test
    @org.junit.jupiter.api.Order(1)
    public void testCreateOrderAndCheckoutInvalidCustomerId() {
        Date deliveryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        OrderCreationDto request = new OrderCreationDto(99, deliveryDate);

        ResponseEntity<ErrorDto> response = client.postForEntity("/checkout", request, ErrorDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No customer found with id 99", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    public void testCreateOrderAndCheckoutNullDelivDate() {
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), null);

        ResponseEntity<ErrorDto> response = client.postForEntity("/checkout", request, ErrorDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No delivery date found", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    public void testCreateOrderAndCheckoutInvalidDelivDate() {
        Date currentDate = new Date(System.currentTimeMillis());
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), currentDate);

        ResponseEntity<ErrorDto> response = client.postForEntity("/checkout", request, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("The delivery date " + request.getDelivDate() + " can not be placed less than 24 hours after the order date " + currentDate, responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    public void testCreateOrderAndCheckoutInvalidCart() {
        Date deliveryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        OrderCreationDto request = new OrderCreationDto(testCustomer.getId(), deliveryDate);

        Cart cart = cartRepository.findByAccount_Id(testCart.getId());
        cartItemRepository.deleteAll(cartItemRepository.findByCartItemId_Cart(cart));

        ResponseEntity<ErrorDto> response = client.postForEntity("/checkout", request, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Cannot checkout with an empty cart", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    public void testCreateOrderAndCheckoutInvalidQuantity() {
        testCartItem1.setQuantity(0);
        cartItemRepository.save(testCartItem1);

        Date deliveryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        OrderCreationDto dto = new OrderCreationDto(testCustomer.getId(), deliveryDate);

        ResponseEntity<ErrorDto> response = client.postForEntity("/checkout", dto, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Cart contains an invalid quantity: 0", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    public void testCreateOrderAndCheckoutItemTypeNull() {
        testItemType1 = null;
        itemTypeRepository.save(testItemType1);

        Date deliveryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        OrderCreationDto dto = new OrderCreationDto(testCustomer.getId(), deliveryDate);

        ResponseEntity<ErrorDto> response = client.postForEntity("checkout", dto, ErrorDto.class);

        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Cart contains an item with no item type", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    public void testCreateOrderAndCheckoutNotEnoughStock() {
        testCartItem1.setQuantity(testItemType1.getStock() + 5); // too many
        cartItemRepository.save(testCartItem1);

        Date deliveryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        OrderCreationDto dto = new OrderCreationDto(testCustomer.getId(), deliveryDate);

        ResponseEntity<ErrorDto> response = client.postForEntity("/checkout", dto, ErrorDto.class);

        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Not enough stock for itemType id " + testItemType1.getId() +
        ". Requested " + testItemType1.getStock() + 5 + ", available " + testItemType1.getStock() + 5,responseBody.getErrors().toString()) ;
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    public void testValidFindOrderById() {


        // Act
        ResponseEntity<OrderResponseDto> response = client.getForEntity("/order/" + validOrderId, OrderResponseDto.class);
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned

        Order order = orderRepository.findById(validOrderId).orElse(null);
        assertEquals(responseBody.getDelivDate(), order.getDelivDate());
        assertEquals(responseBody.getOrderDate(), order.getOrderDate());
        assertEquals(responseBody.getOrderState(), order.getOrderState());
        assertEquals(responseBody.getId(), order.getId());
        assertEquals(responseBody.getCustomerId(), order.getCustomer().getId());
        assertEquals(responseBody.getAssigneeId(), order.getAssignee().getId());
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    public void testValidAssignEmployeeByManager() {

        Order order = orderRepository.findById(validOrderId).orElse(null);

        // Act
        String url = "/order/" + validOrderId + "/assign"
                + "?employeeId=" + testEmployee.getId()
                + "&accountId=" + testManager.getAccount().getId();

        ResponseEntity<OrderResponseDto> response = client.exchange(
                url, HttpMethod.PUT, null, OrderResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the assignment was successful
        assertNotNull(order.getAssignee());
        assertEquals(testEmployee.getId(), order.getId());

    }

    @Test
    @org.junit.jupiter.api.Order(10)
    public void testValidAssignEmployeeByEmployee() {

        Order order = orderRepository.findById(validOrderId).orElse(null);


        // Act
        String url = "/order/" + validOrderId + "/assign"
                + "?employeeId=" + testEmployee.getId()
                + "&accountId=" + testEmployee.getAccount().getId();

        ResponseEntity<OrderResponseDto> response = client.exchange(
                url, HttpMethod.PUT, null, OrderResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the assignment was successful
        assertNotNull(order.getAssignee());
        assertEquals(testEmployee.getId(), order.getId());

    }

    @Test
    @org.junit.jupiter.api.Order(11)
    public void testAssignEmployeeOrderNull() {

        // Act
        String url = "/order/" + 9999 + "/assign"
                + "?employeeId=" + testEmployee.getId()
                + "&accountId=" + testEmployee.getAccount().getId();

        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No order found with id 9999", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    public void testAssignEmployeeAccountNull() {

        // Act
        String url = "/order/" + validOrderId + "/assign"
                + "?employeeId=" + testEmployee.getId()
                + "&accountId=" + 9999;

        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No account doing the action found with id 9999", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    public void testAssignEmployeeCustomerMakingTheChange() {

        // Act
        String url = "/order/" + validOrderId + "/assign"
                + "?employeeId=" + testEmployee.getId()
                + "&accountId=" + testCustomer.getId();

        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("The order cannot be assigned to the employee with id " + testEmployee.getId() + " as a customer is making the change.", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(14)
    public void testAssignEmployeeEmployeeNotFound() {

        // Act
        String url = "/order/" + validOrderId + "/assign"
                + "?employeeId=" + 9999
                + "&accountId=" + testManager.getAccount().getId();

        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No employee found with id 9999", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    public void testAssignEmployeeEmployeeIsCustomer() {

        Employee employeeIsCustomer = new Employee(testCustomer);
        employeeRepository.save(employeeIsCustomer);

        // Act
        String url = "/order/" + validOrderId + "/assign"
                + "?employeeId=" + employeeIsCustomer.getId()
                + "&accountId=" + testManager.getAccount().getId();

        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("The order cannot be assigned to the employee with id " + employeeIsCustomer.getId() + " as he is the customer.", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    public void testAssignEmployeeByAnotherEmployee() {

        Account accountNewEmployee = new Account("Dora", "dora@gmail.com", "ewffce3f%%0", "4000 McGill", 0);
        accountRepository.save(accountNewEmployee);
        Employee newEmployee = new Employee(accountNewEmployee);
        employeeRepository.save(newEmployee);

        // Act
        String url = "/order/" + validOrderId + "/assign"
                + "?employeeId=" + newEmployee.getId()
                + "&accountId=" + testEmployee.getAccount().getId();

        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("The order cannot be assigned to the employee by another employee.", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(17)
    public void testValidUpdateStatusOrderedToInPreparation() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "InPreparation"
                + "&accountId=" + testEmployee.getAccount().getId();


        ResponseEntity<OrderResponseDto> response = client.exchange(
                url,
                HttpMethod.PUT, null, OrderResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(Order.OrderState.InPreparation, order.getOrderState());
    }

    @Test
    @org.junit.jupiter.api.Order(18)
    public void testValidUpdateStatusInPreparationToPrepared() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Prepared"
                + "&accountId=" + testEmployee.getAccount().getId();


        ResponseEntity<OrderResponseDto> response = client.exchange(
                url,
                HttpMethod.PUT, null, OrderResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(Order.OrderState.Prepared, order.getOrderState());
    }


    @Test
    @org.junit.jupiter.api.Order(19)
    public void testValidUpdateStatusPreparedToDelivered() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Delivered"
                + "&accountId=" + testEmployee.getAccount().getId();


        ResponseEntity<OrderResponseDto> response = client.exchange(
                url,
                HttpMethod.PUT, null, OrderResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(Order.OrderState.Delivered, order.getOrderState());
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    public void testValidUpdateStatusToCancelled() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Cancelled"
                + "&accountId=" + testManager.getAccount().getId();


        ResponseEntity<OrderResponseDto> response = client.exchange(
                url,
                HttpMethod.PUT, null, OrderResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(Order.OrderState.Cancelled, order.getOrderState());

        Date currentDate = new Date(System.currentTimeMillis());
        assertTrue((((order.getDelivDate().getTime() - currentDate.getTime()) / (60 * 60 * 1000)) >= 24));

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

    @Test
    @org.junit.jupiter.api.Order(21)
    public void testUpdateStatusForOrderThatDoesNotExist() {


        String url = "/order/" + 999999 + "/status"
                + "?orderState=" + "Delivered"
                + "&accountId=" + testManager.getAccount().getId();


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No order found with id 999999", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(22)
    public void testUpdateStatusToCancellationByAccountThatDoesNotExist() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Cancelled"
                + "&accountId=" + 99999;


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No account found with id 9999", responseBody.getErrors().toString());
        assertTrue(order.getOrderState() != Order.OrderState.Cancelled);
    }

    @Test
    @org.junit.jupiter.api.Order(23)
    public void testUpdateStatusToCancellationLess24HoursToDelivery() {
        Order order = orderRepository.findById(validOrderId).orElse(null);
        order.setDelivDate(new Date(System.currentTimeMillis() + 12 * 60 * 60 * 1000));
        orderRepository.save(order);

        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Cancelled"
                + "&accountId=" + testManager.getAccount().getId();


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("The order cannot be cancelled less than 24 hours before the delivery date " + order.getDelivDate(), responseBody.getErrors().toString());
        assertTrue(order.getOrderState() != Order.OrderState.Cancelled);

    }

    @Test
    @org.junit.jupiter.api.Order(24)
    public void testUpdateStatusToCancellationByEmployee() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Cancelled"
                + "&accountId=" + testEmployee.getAccount().getId();


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("The order cannot be cancelled by an employee", responseBody.getErrors().toString());
        assertTrue(order.getOrderState() != Order.OrderState.Cancelled);

    }

    @Test
    @org.junit.jupiter.api.Order(25)
    public void testUpdateStatusByCustomer() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "InPreparation"
                + "&accountId=" + testCustomer;


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("The order state cannot be updated by a customer", responseBody.getErrors().toString());
        assertTrue(order.getOrderState() == Order.OrderState.Ordered);

    }

    @Test
    @org.junit.jupiter.api.Order(26)
    public void testUpdateStatusFromOrderedToPrepared() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Prepared"
                + "&accountId=" + testManager.getAccount().getId();


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Order with id " + validOrderId + " cannot be switched to " + Order.OrderState.Prepared.toString() + " because it is not InPreparation", responseBody.getErrors().toString());
        assertTrue(order.getOrderState() == Order.OrderState.Ordered);

    }

    @Test
    @org.junit.jupiter.api.Order(27)
    public void testUpdateStatusFromOrderedToDelivered() {
        Order order = orderRepository.findById(validOrderId).orElse(null);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Delivered"
                + "&accountId=" + testManager.getAccount().getId();


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Order with id " + validOrderId + " cannot be switched to " + Order.OrderState.Delivered.toString() + " because it is not Prepared", responseBody.getErrors().toString());
        assertTrue(order.getOrderState() == Order.OrderState.Ordered);

    }


    @Test
    @org.junit.jupiter.api.Order(28)
    public void testUpdateStatusFromInPreparationToDelivered() {
        Order order = orderRepository.findById(validOrderId).orElse(null);
        order.setOrderState(Order.OrderState.InPreparation);
        orderRepository.save(order);


        String url = "/order/" + validOrderId + "/status"
                + "?orderState=" + "Delivered"
                + "&accountId=" + testManager.getAccount().getId();


        ResponseEntity<ErrorDto> response = client.exchange(
                url, HttpMethod.PUT, null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Order with id " + validOrderId + " cannot be switched to " + Order.OrderState.Delivered.toString() + " because it is not Prepared", responseBody.getErrors().toString());
        assertTrue(order.getOrderState() == Order.OrderState.InPreparation);

    }

    @Test
    @org.junit.jupiter.api.Order(29)
    public void testValidViewOrders() {
        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/order?userId=" + testManager.getAccount().getId(), OrderResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(1, responseBody.length);
        assertEquals(validOrderId, responseBody[0].getId());
    }

    @Test
    @org.junit.jupiter.api.Order(30)
    public void testInvalidViewOrders() {

        ResponseEntity<ErrorDto> response = client.getForEntity(
                "/order?userId=" + testCustomer.getId(), null, ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Customers may not view of all of the orders in the system", responseBody.getErrors().toString());
    }


    @Test
    @org.junit.jupiter.api.Order(31)
    public void testValidViewOrdersByDateAscending() {

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);

        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/order-date?userId=" + testManager.getAccount().getId()  +"&filterDirection=" + "ASC", OrderResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(validOrderId, responseBody[0].getId());
        assertEquals(order2.getId(), responseBody[1].getId());
    }

    @Test
    @org.junit.jupiter.api.Order(32)
    public void testValidViewOrdersByDateADescending() {

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);

        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/order-date?userId=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(validOrderId, responseBody[1].getId());
        assertEquals(order2.getId(), responseBody[0].getId());
    }

    @Test
    @org.junit.jupiter.api.Order(33)
    public void testInvalidViewOrdersByDate() {

        ResponseEntity<ErrorDto> response = client.getForEntity(
                "/order-date?userId=" + testCustomer.getId()  +"&filterDirection=" + "descending", ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Customers may not view of all of the orders in the system", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(34)
    public void testValidViewOrdersByCustomerDescending() {

        Account customer2 = new Account("albert", "albert@gmail.com", "revfgnr33e!10", "6000 McGill", 0);
        accountRepository.save(customer2);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);

        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, customer2, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/customer?userId=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(validOrderId, responseBody[1].getId());
        assertEquals(order2.getId(), responseBody[0].getId());
    }


    @Test
    @org.junit.jupiter.api.Order(35)
    public void testValidViewOrdersByCustomerAscending() {

        Account customer2 = new Account("albert", "albert@gmail.com", "revfgnr33e!10", "6000 McGill", 0);
        accountRepository.save(customer2);

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);

        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Ordered, customer2, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/customer?userId=" + testManager.getAccount().getId()  +"&filterDirection=" + "asc", OrderResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(validOrderId, responseBody[0].getId());
        assertEquals(order2.getId(), responseBody[1].getId());
    }

    @Test
    @org.junit.jupiter.api.Order(36)
    public void testInvalidViewOrdersByCustomer() {

        ResponseEntity<ErrorDto> response = client.getForEntity(
                "/customer?userId=" + testCustomer.getId()  +"&filterDirection=" + "descending", ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Customers may not view of all of the orders in the system", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(37)
    public void testValidViewOrdersByOrderStateDescending() {

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);

        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Prepared, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/status?userId=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(validOrderId, responseBody[1].getId());
        assertEquals(order2.getId(), responseBody[0].getId());
    }


    @Test
    @org.junit.jupiter.api.Order(38)
    public void testValidViewOrdersByOrderStateAsc() {

        Date dateOrder = new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000);
        Date dateDeliver = new Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000);

        Order order2 = new Order(dateOrder, dateDeliver, Order.OrderState.Prepared, testCustomer, null);
        orderRepository.save(order2);

        // Act
        ResponseEntity<OrderResponseDto[]> response = client.getForEntity(
                "/status?userId=" + testManager.getAccount().getId()  +"&filterDirection=" + "asc", OrderResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(validOrderId, responseBody[0].getId());
        assertEquals(order2.getId(), responseBody[1].getId());
    }

    @Test
    @org.junit.jupiter.api.Order(39)
    public void testInvalidViewOrdersByStatus() {

        ResponseEntity<ErrorDto> response = client.getForEntity(
                "/status?userId=" + testCustomer.getId()  +"&filterDirection=" + "descending", ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Customers may not view of all of the orders in the system", responseBody.getErrors().toString());
    }

}
