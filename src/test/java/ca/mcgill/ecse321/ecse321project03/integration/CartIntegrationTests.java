package ca.mcgill.ecse321.ecse321project03.integration;
// import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ca.mcgill.ecse321.ecse321project03.dto.CartCreationDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartRepository;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.dto.CartDto;



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

    private int createdCartId;
    private Account account;

    @BeforeAll
    @AfterAll
    public void clearDatabase() {
        cartRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @BeforeAll
    public void createAccount() {

        account = new Account();
        account.setUsername("Maria Klinski");
        account.setEmail("mariaklinski@mail.com");
        account.setPassword("Maria123#");
        account.setAddress("123 Peel St");

        account = accountRepository.save(account);
    }

    @Test
    @Order(0)
    public void testCreateValidCart() {

        // Arrange
        CartCreationDto body = new CartCreationDto(account);

        // Act
        ResponseEntity<CartDto> response =
                client.postForEntity("/cart", body, CartDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0, "the ID should be a positive integer");
        this.createdCartId = response.getBody().getId();
        assertEquals(account.getId(), response.getBody().getAccount().getId());
    }

    @Test
    @Order(1)
    public void testFindCartByValidId() {

        String url = String.format("/cart/%d", createdCartId);

        ResponseEntity<CartDto> response =
                client.getForEntity(url, CartDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdCartId, response.getBody().getId());
    }
}