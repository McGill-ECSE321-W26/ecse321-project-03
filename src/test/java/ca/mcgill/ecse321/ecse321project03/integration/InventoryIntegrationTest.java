package ca.mcgill.ecse321.ecse321project03.integration;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemTypeDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.repository.*;
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
public class InventoryIntegrationTest {


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

    // RESTOCK

    /**
     * Method that validates the restock of items
     * @author Wissam Abchiche
     */
    @Test
    public void testValidRestock() {

        // Act
        ResponseEntity<ItemTypeDto> response = client.postForEntity("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + testManager.getAccount().getId(), null, ItemTypeDto.class);

        // Verify the status code is HTTP.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify that the stock was updated
        ItemTypeDto itemTypeDto =  response.getBody();
        assertEquals(13, itemTypeDto.getStock());

    }


    /**
     * Method which verifies that the invalid action corresponds to a certain HttpStatus and an expectedError
     * @author Wissam Abchiche
     * @param url the url of the restock
     * @param expectedHttpStatus    the expected HTTP status code of the response
     * @param expectedErrorMessage  the expected error message in the response body
     */
    private void invalidTestForCreateRestock(String url, HttpStatus expectedHttpStatus, String expectedErrorMessage) {
        // Verify the status code is the expected one
        ResponseEntity<ErrorDto> response = client.postForEntity(url, null, ErrorDto.class);
        assertEquals(expectedHttpStatus, response.getStatusCode());

        // Verify that the error message returned is what is expected
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedErrorMessage, responseBody.getErrors().get(0));
    }


    /**
     * Method that invalidates the restock when given an invalid account id
     * @author Wissam Abchiche
     */
    @Test
    public void testRestockInvalidAccountId() {

        invalidTestForCreateRestock("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + -1, HttpStatus.NOT_FOUND, "No account managing the stock found with id -1");
    }


    /**
     * Method that invalidates the restock when done by customer
     * @author Wissam Abchiche
     */
    @Test
    public void testRestockInvalidByCustomer() {
        invalidTestForCreateRestock("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + testCustomer.getId(), HttpStatus.FORBIDDEN, "A customer cannot manage the stock");
    }



    /**
     * Method that invalidates the restock when done by an employee
     * @author Wissam Abchiche
     */
    @Test
    public void testRestockInvalidByEmployee() {

        invalidTestForCreateRestock("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + testEmployee.getAccount().getId(), HttpStatus.FORBIDDEN, "A employee cannot manage the stock");
    }


    /**
     * Method that invalidates the restock when invalid quantity
     * @author Wissam Abchiche
     */
    @Test
    public void testRestockInvalidQuantity() {
        invalidTestForCreateRestock("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 0
                + "&accountId=" + testManager.getAccount().getId(), HttpStatus.BAD_REQUEST, "Restock quantity must be positive");
    }


    /**
     * Method that invalidates the restock when invalid item type id
     * @author Wissam Abchiche
     */
    @Test
    public void testRestockInvalidWithNoItemType() {

        invalidTestForCreateRestock("/restock"
                + "?itemTypeId=" + -1
                + "&quantityToAdd=" + 2
                + "&accountId=" + testManager.getAccount().getId(), HttpStatus.NOT_FOUND, "No item type found with id -1");
    }


    // STOCK STATUS

    /**
     * Method that validates the get stock status action when stock is 0
     * @author Wissam Abchiche
     */
    @Test
    public void testValidGetStockStatusStockEqual0() {

        // Update stock of item type to 0 and save to repo
        testItemType1.setStock(0);
        itemTypeRepository.save(testItemType1);

        // Act
        ResponseEntity<String> response = client.getForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(), String.class);

        // Validate the status code is HTTP.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Validate if the message is correct
        String status =  response.getBody();
        assertEquals("Out of Stock", status);


    }

    /**
     * Method that validates the get stock status action when stock is less than 10
     * @author Wissam Abchiche
     */
    @Test
    public void testValidGetStockStatusLessThan10() {
        // Update stock of item type to 9 and save to repo
        testItemType1.setStock(9);
        itemTypeRepository.save(testItemType1);

        // Act
        ResponseEntity<String> response =client.getForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(),  String.class);

        // Validate the status code is HTTP.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Validate if the message is correct
        String status =  response.getBody();
        assertEquals("Almost Out of Stock! Only " + 9 + " left!!!", status);
    }

    /**
     * Method that validates the get stock status action when stock is more than 9
     * @author Wissam Abchiche
     */
    @Test
    public void testValidGetStockStatusMoreThan9() {
        // Update stock of item type to 10 and save to repo
        testItemType1.setStock(10);
        itemTypeRepository.save(testItemType1);

        // Act
        ResponseEntity<String> response = client.getForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(),  String.class);

        // Validate the status code is HTTP.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Validate if the message is correct
        String status =  response.getBody();
        assertEquals("In Stock", status);
    }

    /**
     * Method that invalidates the get stock status action when the account performing the action doesn't exist
     * @author Wissam Abchiche
     */
    @Test
    public void testGetStockStatusInvalidAccountId() {

        ResponseEntity<ErrorDto> response = client.getForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + -1,  ErrorDto.class);

        // Validate the status code is HTTP.NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();

        // Validate if the message is correct
        assertNotNull(responseBody);
        assertEquals("No account found with id -1", responseBody.getErrors().get(0));
    }


    /**
     * Method that invalidates the get stock status action when the item type  doesn't exist
     * @author Wissam Abchiche
     */
    @Test
    public void testGetStockStateInvalidWithNoItemType() {

        ResponseEntity<ErrorDto> response = client.getForEntity("/stock-status"
                + "?itemTypeId=" + -1
                + "&accountId=" + testManager.getAccount().getId(), ErrorDto.class);

        // Validate the status code is HTTP.NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();

        // Validate if the message is correct
        assertNotNull(responseBody);
        assertEquals("No item type found with id -1", responseBody.getErrors().get(0));
    }


    // GET STOCK

    /**
     * Method that validates the get stock action
     * @author Wissam Abchiche
     */
    @Test
    public void testValidGetStock() {
        // Act
        ResponseEntity<Integer> response = client.getForEntity("/stock-level"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(),  Integer.class);

        // Validate the status code is HTTP.OK
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Validate if the found stock is correct
        int stock =  response.getBody();
        assertEquals(10, stock);

    }

    /**
     * Method that invalidates the get stock action when done by an account that doesn't exist
     * @author Wissam Abchiche
     */
    @Test
    public void testGetStockInvalidAccountId() {

        ResponseEntity<ErrorDto> response = client.getForEntity("/stock-level"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + -1, ErrorDto.class);

        // Validate the status code is HTTP.NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();

        // Validate if the message is correct
        assertNotNull(responseBody);
        assertEquals("No account found with id -1", responseBody.getErrors().toString());
    }



    /**
     * Method that invalidates the get stock  action when no item type exists
     * @author Wissam Abchiche
     */
    @Test
    public void testGetStockInvalidWithNoItemType() {

        ResponseEntity<ErrorDto> response = client.getForEntity("/stock-level"
                + "?itemTypeId=" + -1
                + "&accountId=" + testManager.getAccount().getId(),  ErrorDto.class);

        // Validate the status code is HTTP.NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();

        // Validate if the message is correct
        assertNotNull(responseBody);
        assertEquals("No item type found with id -1", responseBody.getErrors().toString());
    }


    /**
     * Method that validates the get all items action
     * @author Wissam Abchiche
     */
    @Test
    public void testValidGetAllItemTypes() {
        // Act
        ResponseEntity<ItemTypeDto[]> response = client.getForEntity("/item-types"
                + "?accountId=" + testManager.getAccount().getId(), ItemTypeDto[].class);

        // Assert
        assertNotNull(response);

        // Validate the status code is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify that the number of item types is 2
        ItemTypeDto[] itemTypeDtos =  response.getBody();
        assertEquals(2, itemTypeDtos.length);

        // Verify that the first created testItempType is at position 0 of the array and the second one at position 1
        assertEquals(itemTypeDtos[0].getId(), testItemType1.getId());
        assertEquals(itemTypeDtos[1].getId(), testItemType2.getId());
    }


    /**
     * Method that invalidates the get items  action when done by an account that doesn't exist
     * @author Wissam Abchiche
     */
    @Test
    public void testGetAllItemTypesInvalidAccountId() {

        ResponseEntity<ErrorDto> response = client.getForEntity("/item-types"
                + "&accountId=" + -1, ErrorDto.class);

        // Validate the status code is NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();

        // Validate if the message is correct
        assertNotNull(responseBody);
        assertEquals("No account found with id 9999", responseBody.getErrors().toString());
    }
}






