package ca.mcgill.ecse321.ecse321project03.integration;
// import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.AccountResponseDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// source: tutorial 7
// author: Maria Klinski

// embedded server will start at a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// testsd will be run based on @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

// instance will be shared across all methods
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class AccountIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    private int createdAccountId;

    private static final String VALID_USERNAME = "Maria Klinski";
    private static final String VALID_EMAIL = "mariaklinski@gmail.com";
    private static final String VALID_PASSWORD = "Maria123#";
    private static final String VALID_ADDRESS = "123 Peel St, Montreal, QC";

    @Autowired
    private AccountRepository accountRepository;

    @BeforeAll
    @AfterAll
    public void cleanDatabase() {
        accountRepository.deleteAll();
    }


    @Test
    @Order(0)
    public void testCreateValidAccount() {
        // Arrange
        AccountCreationDto body = new AccountCreationDto(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD, VALID_ADDRESS);

        // Act
        ResponseEntity<AccountResponseDto> response = client.postForEntity("/account", body, AccountResponseDto.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0, "the ID should be a positive integer");
        this.createdAccountId = response.getBody().getId();
        assertEquals(body.getUsername(), response.getBody().getUsername());
        assertEquals(body.getEmail(), response.getBody().getEmail());
        assertEquals(body.getAddress(), response.getBody().getAddress());

    }

    // password too short
    @Test
    @Order(1)
    public void testCreateAccountWithShortPassword() {
        // Arrange
        String shortPassword = "Short1!";
        AccountCreationDto body = new AccountCreationDto(VALID_USERNAME, VALID_EMAIL, shortPassword, VALID_ADDRESS);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/account", body, ErrorDto.class);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("Password must be at least 8 characters long."), response.getBody().getErrors());

    }

    // password without number
    @Test
    @Order(2)
    public void testCreateAccountPasswordWithoutNumber() {
        // Arrange
        String shortPassword = "ShortPass!";
        AccountCreationDto body = new AccountCreationDto(VALID_USERNAME, VALID_EMAIL, shortPassword, VALID_ADDRESS);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/account", body, ErrorDto.class);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("Password must be at least 8 characters long."), response.getBody().getErrors());

    }

    // password without special character
    @Test
    @Order(3)
    public void testCreateAccountPasswordWithoutSpecialCharacter() {
        // Arrange
        String shortPassword = "ShortPass1";
        AccountCreationDto body = new AccountCreationDto(VALID_USERNAME, VALID_EMAIL, shortPassword, VALID_ADDRESS);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/account", body, ErrorDto.class);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("Password must contain at least one special character."), response.getBody().getErrors());
    }

    // password without uppercase letter
    @Test
    @Order(4)
    public void testCreateAccountPasswordWithoutUppercaseLetter() {
        // Arrange
        String shortPassword = "shortpass1!";
        AccountCreationDto body = new AccountCreationDto(VALID_USERNAME, VALID_EMAIL, shortPassword, VALID_ADDRESS);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/account", body, ErrorDto.class);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("Password must contain at least one uppercase letter."), response.getBody().getErrors());
    }

    //password without lowercase letter
    @Test
    @Order(5)
    public void testCreateAccountPasswordWithoutLowercaseLetter() {
        // Arrange
        String shortPassword = "SHORTPASS1!";
        AccountCreationDto body = new AccountCreationDto(VALID_USERNAME, VALID_EMAIL, shortPassword, VALID_ADDRESS);

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/account", body, ErrorDto.class);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("Password must contain at least one lowercase letter."), response.getBody().getErrors());
    }


    @Test
    @Order(6)
    public void testFindAccountByValidId() {
        // Arrange
        String url = String.format("/account/%d", this.createdAccountId);

        // Act
        ResponseEntity<AccountResponseDto> response = client.getForEntity(url, AccountResponseDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(this.createdAccountId, response.getBody().getId());
        assertEquals(VALID_USERNAME, response.getBody().getUsername());
        assertEquals(VALID_EMAIL, response.getBody().getEmail());
        assertEquals(VALID_ADDRESS, response.getBody().getAddress());
    }
}