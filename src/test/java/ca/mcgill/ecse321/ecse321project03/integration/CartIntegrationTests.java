package ca.mcgill.ecse321.ecse321project03.integration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import ca.mcgill.ecse321.ecse321project03.dto.CartCreationDto;
import ca.mcgill.ecse321.ecse321project03.repository.CartRepository;
import ca.mcgill.ecse321.ecse321project03.dto.CartDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemDto;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;

import static org.junit.jupiter.api.Assertions.*;


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

    @Autowired
    private AccountRepository accountRepository;
    
    private int testUserId;

    @BeforeAll
    @AfterAll
    public void clearDatabase() {
        cartRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        cartRepository.deleteAll();
        accountRepository.deleteAll();

        Account account = new Account("Test", "test@gmail.com", "Password123!", "123 Street", 0);
        accountRepository.save(account);

        testUserId = account.getId();
    }

    /*
        Valid test for endpoint POST create cart
        @author: Maria Klinski
     */
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

    /*
        Invalid test for endpoint POST create cart, empty
        @author: Maria Klinski
     */
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
        Valid test for endpoint POST add item to cart
        @author: Maria Klinski
     */
    @Test
    @Order(2)
    public void testAddItemToCartValid() {

        // Arrange
        //ItemTypeDto itemTypeDto = new ItemTypeDto(1, "Test Item", Color.RED, Size.M);
        CartItemDto body = new CartItemDto();
        body.setQuantity(2);

        // Act
        ResponseEntity<CartItemDto> response =
                client.postForEntity("/carts/" + testUserId + "/items", body, CartItemDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getQuantity());
    }
    /*
        Invalid test for endpoint POST add item to cart, invalid userID
        @author: Maria Klinski
     */
    @Test
    @Order(3)
    public void testAddItemToCartInvalid() {

        // Arrange
        int invalidUserId = 1000;
        CartItemDto body = new CartItemDto();
        body.setQuantity(1);


        // Act
        ResponseEntity<String> response =
                client.postForEntity("/carts/" + invalidUserId + "/items", body, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /*
        Valid test for endpoint GET find cart total price
        @author: Maria Klinski

     */
    @Test
    @Order(4)
    public void testFindCartTotalPriceValid() {
        // Act
        ResponseEntity<Double> response =
                client.getForEntity("/carts/" + testUserId + "/total", Double.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /*
        Invalid test for endpoint GET find cart total price, invalid userID
        @author: Maria Klinski
     */
    @Test
    @Order(5)   
    public void testFindCartTotalPriceInvalid() {
        int invalidUserId = 1000;
        
        // Act
        ResponseEntity<String> response =
                client.getForEntity("/carts/" + invalidUserId + "/total", String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /*
        Valid test for endpoint DELETE delete cart
        @author: Maria Klinski
     */
    @Test
    @Order(6)
    public void testDeleteCartValid() {
        // Act
        ResponseEntity<Void> response =
                client.exchange("/carts/" + testUserId, HttpMethod.DELETE, null, Void.class);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    /*
        Invalid test for endpoint DELETE delete cart, invalid userID
        @author: Maria Klinski
     */
    @Test
    @Order(7)
    public void testDeleteCartInvalid() {
        int invalidUserId = 1000;

        // Act
        ResponseEntity<String> response =
                client.exchange("/carts/" + invalidUserId, HttpMethod.DELETE, null, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}