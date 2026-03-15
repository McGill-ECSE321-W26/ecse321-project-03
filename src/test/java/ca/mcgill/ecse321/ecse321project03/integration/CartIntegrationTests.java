package ca.mcgill.ecse321.ecse321project03.integration;
import ca.mcgill.ecse321.ecse321project03.dto.*;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

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

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    
    private Account testAccount;

    @BeforeAll
    @AfterAll
    public void clearDatabase() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        itemTypeRepository.deleteAll();
        itemRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        testAccount = new Account("Test", "test@gmail.com", "Password123!", "123 Street", 0);
        testAccount = accountRepository.save(testAccount);
    }

    /*
        Valid test for endpoint POST create cart
        @author: Maria Klinski
     */
    @Test
    @Order(0)
    public void testCreateNewCartValid() {

        // Arrange
        CartCreationDto body = new CartCreationDto(testAccount);

        // Act
        ResponseEntity<CartDto> response =
                client.postForEntity("/carts/" + testAccount.getId(), body, CartDto.class);

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
        Cart existing = new Cart(testAccount);
        cartRepository.save(existing);
        CartCreationDto body = new CartCreationDto(testAccount);


        // Act
        ResponseEntity<ErrorDto> response =
                client.postForEntity("/carts/" + testAccount.getId(), body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
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
        Cart cart = new Cart(testAccount);
        cartRepository.save(cart);

        Item item = new Item("Dress", Item.Tag.Dress, 15);
        itemRepository.save(item);

        ItemType itemType = new ItemType(ItemType.Size.M,ItemType.Color.Green, 10,item);
        itemTypeRepository.save(itemType);

        CartItemDto body = new CartItemDto();
        body.setItemType(new ItemTypeDto(itemType));
        body.setQuantity(2);

        // Act
        ResponseEntity<CartItemDto> response =
                client.postForEntity("/carts/" + testAccount.getId() + "/items", body, CartItemDto.class);

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
        ResponseEntity<ErrorDto> response =
                client.postForEntity("/carts/" + invalidUserId + "/items", body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /*
        Valid test for endpoint GET find cart total price
        @author: Maria Klinski

     */
    @Test
    @Order(4)
    public void testFindCartTotalPriceValid() {
        // Arrange
        Cart cart = new Cart (testAccount);
        cartRepository.save(cart);

        Item item = new Item ("DressA", Item.Tag.Dress, 15);
        itemRepository.save(item);

        ItemType itemType = new ItemType (ItemType.Size.M, ItemType.Color.Green, 10, item);
        itemTypeRepository.save(itemType);

        CartItem.CartItemId cartItemId = new CartItem.CartItemId(itemType, cart);
        CartItem cartItem = new CartItem(cartItemId, 2);
        cartItemRepository.save(cartItem);

        // Act
        ResponseEntity<Double> response =
                client.getForEntity("/carts/" + testAccount.getId() + "/total", Double.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(30.0, response.getBody());
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
        ResponseEntity<ErrorDto> response =
                client.getForEntity("/carts/" + invalidUserId + "/total", ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /*
        Valid test for endpoint DELETE delete cart
        @author: Maria Klinski
     */
    @Test
    @Order(6)
    public void testDeleteCartValid() {
        // Arrange
        Cart cart = new Cart (testAccount);
        cartRepository.save(cart);

        // Act
        ResponseEntity<Void> response =
                client.exchange("/carts/" + testAccount.getId(), HttpMethod.DELETE, null, Void.class);

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
        ResponseEntity<ErrorDto> response =
                client.exchange("/carts/" + invalidUserId, HttpMethod.DELETE, null, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}