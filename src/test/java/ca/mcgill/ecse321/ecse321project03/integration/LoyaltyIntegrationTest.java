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

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoyaltyIntegrationTest {


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
        testCustomer = new Account("Jeane", "jeane@gmail.com", "$fjdgbfdju1234", "1000 McGill", 10);
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

    /**
     * Method which returns the url of redeemLoyalty
     * @author Wissam Abchiche
     */
    private String url(int orderId, int accountId, boolean usePoints) {
        return
                "/orders/redeem-loyalty/" + orderId
                        + "?accountId=" + accountId
                        + "&usePoints=" + usePoints;
    }

    /**
     * Method that validates that the redeem loyalty points with a 10% discount
     * @author Wissam Abchiche
     */
    @Test
    public void testValidRedeemLoyaltyPts_10PercentDiscount() {

        // Create valid order
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48L * 60L * 60L * 1000L);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer, null);
        orderRepository.save(order);

        ResponseEntity<String> response = client.postForEntity(
                url(order.getId(), testCustomer.getId(), true),
                null, String.class);

        // Assert the HTTP status
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String loyaltyObtained =  response.getBody();

        // Verify the loyalty obtained is the desired one
        assertNotNull(loyaltyObtained);
        assertEquals("You saved $10,00 on this order!", loyaltyObtained);

    }


    /**
     * Method that validates that the redeem loyalty points are not used when not wanted
     * @author Wissam Abchiche
     */
    @Test
    public void testValidRedeemLoyaltyPtsNotUsePoints() {

        // Create valid order
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48L * 60L * 60L * 1000L);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer, null);
        orderRepository.save(order);

        ResponseEntity<String> response = client.postForEntity(
                url(order.getId(), testCustomer.getId(), false),
                null, String.class);

        // Assert the HTTP status
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String loyaltyObtained =  response.getBody();

        // Verify the loyalty obtained is the desired one
        assertNotNull(loyaltyObtained);
        assertEquals("Points not used.", loyaltyObtained);
    }

    /**
     * Method that validates that the redeem loyalty points are not used when not enough
     * @author Wissam Abchiche
     */
    @Test
    public void testValidRedeemLoyaltyPtsNotEnoughPoints() {

        // Create valid order
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48L * 60L * 60L * 1000L);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer, null);
        orderRepository.save(order);

        // Set the number of points of test customer to 0
        testCustomer.setLoyaltyPts(0);
        accountRepository.save(testCustomer);


        ResponseEntity<String> response = client.postForEntity(
                url(order.getId(), testCustomer.getId(), true),
                null, String.class);

        // Assert the HTTP status
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String loyaltyObtained =  response.getBody();

        // Verify the loyalty obtained is the desired one
        assertNotNull(loyaltyObtained);
        assertEquals("You don't have enough points to get a discount :(", loyaltyObtained);
    }



    /**
     * Method which verifies that the invalid action corresponds to a certain HttpStatus and an expectedError
     * @author Wissam Abchiche
     * @param url the url of the redeemLoyalty
     * @param expectedHttpStatus    the expected HTTP status code of the response
     * @param expectedErrorMessage  the expected error message in the response body
     */
    private void invalidTestForRedeemLoyaltyPoints(String url, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
        // Verify the status code is the expected one
        ResponseEntity<ErrorDto> response = client.postForEntity(url, null, ErrorDto.class);
        assertEquals(expectedHttpStatus, response.getStatusCode());

        // Verify that the error message returned is what is expected
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedErrorMessage, responseBody.getErrors().get(0));
    }

    /**
     * Method which verifies that the redeem loyalty points doesnt work when invalid account
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignRedeemAccountNull() {

        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48L * 60L * 60L * 1000L);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testCustomer,null);
        orderRepository.save(order);

        invalidTestForRedeemLoyaltyPoints(url(order.getId(), -1, true), HttpStatus.NOT_FOUND, "No person with id -1 found.");

    }


    /**
     * Method which verifies that the redeem loyalty points doesnt work when invalid order
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignRedeemOrderNull() {
        invalidTestForRedeemLoyaltyPoints(url(-1, testCustomer.getId(), true), HttpStatus.NOT_FOUND, "No order with id -1 found.");

    }

    /**
     * Method which verifies that the redeem loyalty points doesn't work when redeem is activated by an account who didn't make the order
     * @author Wissam Abchiche
     */
    @Test
    public void testAssignRedeemDoneByAccountNotMakingRequest() {

        // Create a valid order and save it in the repo
        Date orderDate = new Date(System.currentTimeMillis());
        Date deliveryDate = new Date(System.currentTimeMillis() + 48L * 60L * 60L * 1000L);
        Order order = new Order(orderDate, deliveryDate, Order.OrderState.Ordered, testAccountEmployee,null);
        orderRepository.save(order);

        invalidTestForRedeemLoyaltyPoints(url(order.getId(), testCustomer.getId(), true), HttpStatus.FORBIDDEN,
                "This order does not belong to the account with id " + testCustomer.getId() + ".");

    }

}

