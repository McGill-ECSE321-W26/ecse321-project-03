package ca.mcgill.ecse321.ecse321project03.integration;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemTypeDto;
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

    @Test
    @org.junit.jupiter.api.Order(0)
    public void testValidRestock() {

        // Act
        ResponseEntity<ItemTypeDto> response = client.postForEntity("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + testManager.getAccount().getId(), null, ItemTypeDto.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ItemTypeDto itemTypeDto =  response.getBody();
        assertEquals(13, itemTypeDto.getStock());

        ItemType itemTypeDB = itemTypeRepository.findById(testItemType1.getId()).orElseThrow();
        assertEquals(13, itemTypeDB.getStock());


    }

    @Test
    @org.junit.jupiter.api.Order(1)
    public void testRestockInvalidAccountId() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + 9999, null, ErrorDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No account managing the stock found with id 9999", responseBody.getErrors().toString());
    }
    @Test
    @org.junit.jupiter.api.Order(2)
    public void testRestockInvalidByCustomer() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + testCustomer.getId(), null, ErrorDto.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("A customer cannot manage the stock", responseBody.getErrors().toString());
    }


    @Test
    @org.junit.jupiter.api.Order(3)
    public void testRestockInvalidByEmployee() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 3
                + "&accountId=" + testEmployee.getId(), null, ErrorDto.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("A employee cannot manage the stock", responseBody.getErrors().toString());
    }


    @Test
    @org.junit.jupiter.api.Order(4)
    public void testRestockInvalidQuantity() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/restock"
                + "?itemTypeId=" + testItemType1.getId()
                + "&quantityToAdd=" + 0
                + "&accountId=" + testManager.getAccount().getId(), null, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Restock quantity must be positive", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    public void testRestockInvalidWithNoItemType() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/restock"
                + "?itemTypeId=" + 99999
                + "&quantityToAdd=" + 0
                + "&accountId=" + testManager.getAccount().getId(), null, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No item type found with id 99999", responseBody.getErrors().toString());
    }


    @Test
    @org.junit.jupiter.api.Order(6)
    public void testValidGetStockStatusStockEqual0() {


        testItemType1.setStock(0);
        itemTypeRepository.save(testItemType1);
        // Act
        ResponseEntity<String> response = client.postForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(), null, String.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String status =  response.getBody();
        assertEquals("Out of Stock", status);


    }

    @Test
    @org.junit.jupiter.api.Order(7)
    public void testValidGetStockStatusLessThan10() {
        testItemType1.setStock(9);
        itemTypeRepository.save(testItemType1);

        // Act
        ResponseEntity<String> response = client.postForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(), null, String.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String status =  response.getBody();
        assertEquals("Almost Out of Stock! Only " + 9 + " left!!!", status);
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    public void testValidGetStockStatusMoreThan9() {
        testItemType1.setStock(10);
        itemTypeRepository.save(testItemType1);

        // Act
        ResponseEntity<String> response = client.postForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(), null, String.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String status =  response.getBody();
        assertEquals("In Stock", status);
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    public void testGetStockStatusInvalidAccountId() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/stock-status"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + 9999, null, ErrorDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No account found with id 9999", responseBody.getErrors().toString());
    }



    @Test
    @org.junit.jupiter.api.Order(10)
    public void testGetStockStateInvalidWithNoItemType() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/stock-status"
                + "?itemTypeId=" + 99999
                + "&accountId=" + testManager.getAccount().getId(), null, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No item type found with id 99999", responseBody.getErrors().toString());
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    public void testValidGetStock() {
        // Act
        ResponseEntity<Integer> response = client.postForEntity("/stock-level"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + testManager.getAccount().getId(), null, Integer.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        int stock =  response.getBody();
        assertEquals(10, stock);

    }

    @Test
    @org.junit.jupiter.api.Order(12)
    public void testGetStockInvalidAccountId() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/stock-level"
                + "?itemTypeId=" + testItemType1.getId()
                + "&accountId=" + 9999, null, ErrorDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No account found with id 9999", responseBody.getErrors().toString());
    }



    @Test
    @org.junit.jupiter.api.Order(13)
    public void testGetStockInvalidWithNoItemType() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/stock-level"
                + "?itemTypeId=" + 99999
                + "&accountId=" + testManager.getAccount().getId(), null, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No item type found with id 99999", responseBody.getErrors().toString());
    }


    @Test
    @org.junit.jupiter.api.Order(14)
    public void testValidGetAllItemTypes() {
        // Act
        ResponseEntity<ItemTypeDto[]> response = client.postForEntity("/item-types"
                + "?accountId=" + testManager.getAccount().getId(), null,ItemTypeDto[].class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ItemTypeDto[] itemTypeDtos =  response.getBody();
        assertEquals(2, itemTypeDtos.length);

        assertEquals(itemTypeDtos[0].getId(), testItemType1.getId());
        assertEquals(itemTypeDtos[1].getId(), testItemType2.getId());
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    public void testGetAllItemTypesInvalidAccountId() {

        ResponseEntity<ErrorDto> response = client.postForEntity("/item-types"
                + "&accountId=" + 9999, null, ErrorDto.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("No account found with id 9999", responseBody.getErrors().toString());
    }
}






