package ca.mcgill.ecse321.ecse321project03.integration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ca.mcgill.ecse321.ecse321project03.dto.CartCreationDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartRepository;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender.Size;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.CartItem;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.dto.CartDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemTypeDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemDto;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;

// source: tutorial 7
// author: Maria Klinski

// embedded server will start at a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// testsd will be run based on @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

// instance will be shared across all methods
@TestInstance(TestInstance.Lifecycle.PER_CLASS)


public class CartIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private CartRepository cartRepository;

    
    private final int testUserId = 15;


    @BeforeAll
    @AfterAll
    public void clearDatabase() {
        cartRepository.deleteAll();
    }

    // POST: Create a new valid cart
    @Test
    @Order(0)
    public void testCreateNewCartValid() {

        // Arrange
        CartCreationDto body = new CartCreationDto();

        // Act
        ResponseEntity<CartDto> response =
                client.postForEntity("/carts/" + testUserId, body, CartDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0, "the ID should be a positive integer");
        
    }

    // POST: Create a new cart
    @Test
    @Order(1)
    public void testCreateNewCartInvalid() {

        // Arrange
        CartCreationDto body = new CartCreationDto();

        // Act
        ResponseEntity<ErrorDto> response =
                client.postForEntity("/carts/" + testUserId, body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
    }
/* 
    @Test
    @Order(2)
    public void testAddItemToCartValid() {

        // Arrange
        ItemTypeDto itemTypeDto = new ItemTypeDto(1, "Test Item", Color.RED, Size.M);
        CartItemDto cartItemDto = new CartItemDto(itemTypeDto, 2);


        CartCreationDto testUserId = new CartCreationDto(testUserId);

        // Act
        ResponseEntity<CartItemDto> response =
                client.postForEntity("/carts/" + testUserId + "/items", body, CartItemDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Item", response.getBody().getItemType());
        assertEquals(2, response.getBody().getQuantity());
    }
*/
    @Test
    @Order(3)
    public void testAddItemToCartInvalid() {

        // Arrange
        CartItemDto body = new CartItemDto();

        // Act
        ResponseEntity<String> response =
                client.postForEntity("/carts/" + testUserId + "/items", body, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("User with id " + testUserId + " does not have a cart"));
    }


    @Test
    @Order(4)
    public void testFindCartTotalPriceValid() {
        // Act
        ResponseEntity<Double> response =
                client.getForEntity("/carts/" + testUserId + "/total", Double.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(20.0, response.getBody(), 0.001);
    }

    @Test
    @Order(5)   
    public void testFindCartTotalPriceInvalid() {
        // Act
        ResponseEntity<String> response =
                client.getForEntity("/carts/" + testUserId + "/total", String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("User with id " + testUserId + " does not have a cart"));
    }

    @Test
    @Order(6)
    public void testDeleteCartValid() {
        // Act
        ResponseEntity<Void> response =
                client.exchange("/carts/" + testUserId, HttpMethod.DELETE, null, Void.class);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}