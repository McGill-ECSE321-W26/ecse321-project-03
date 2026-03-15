package ca.mcgill.ecse321.ecse321project03.integration;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderInfoIntegrationTest {


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
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        employeeRepository.deleteAll();
        managerRepository.deleteAll();
        roleRepository.deleteAll();
        itemTypeRepository.deleteAll();
        itemRepository.deleteAll();
        accountRepository.deleteAll();
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
                "/orders/order?userID=" + testManager.getAccount().getId(), OrderResponseDto[].class);


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
                "/orders/order?userID=" + testCustomer.getId(), ErrorDto.class);

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
                "/orders/order-date?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "ASC", OrderResponseDto[].class);


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
                "/orders/order-date?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);


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
        invalidViewOrders("/orders/order-date?userID=" + testCustomer.getId()  +"&filterDirection=" + "descending", HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");
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
                "/orders/order-customer?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);


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
                "/orders/order-customer?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "asc", OrderResponseDto[].class);


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

        invalidViewOrders("/orders/order-customer?userID=" + testCustomer.getId()  +"&filterDirection=" + "descending", HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");

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
                "/orders/order-status?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "descending", OrderResponseDto[].class);


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
                "/orders/order-status?userID=" + testManager.getAccount().getId()  +"&filterDirection=" + "asc", OrderResponseDto[].class);


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
        invalidViewOrders("/orders/order-status?userID=" + testCustomer.getId()  +"&filterDirection=" + "descending", HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");


    }
}