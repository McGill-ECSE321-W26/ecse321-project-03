package ca.mcgill.ecse321.ecse321project03.integration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.AccountResponseDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// source: tutorial 7
// author: Maria Klinski

// embedded server will start at a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// tests will be run based on @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

// instance will be shared across all methods
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class AccountIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    private int createdAccountId;
    private Account testCustomer;
    private Account testAccountEmployee;
    private Account testAccountManager;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeAll
    @AfterAll
    public void cleanDatabase() {
        accountRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        accountRepository.deleteAll();
        
        // Customer
        testCustomer = new Account("Jeane", "jeane@gmail.com", "$fjdgbfdju1234", "1000 McGill", 0);
        accountRepository.save(testCustomer);

        // Employee
        testAccountEmployee = new Account("Gabriel", "gabriel@gmail.com", "$fjdgb23525", "2000 McGill", 0);
        accountRepository.save(testAccountEmployee);

        // Manager
        testAccountManager = new Account("Joseph", "joseph@gmail.com", "$fjdgb222/115", "3000 McGill", 0);
        accountRepository.save(testAccountManager);

        createdAccountId = testAccountManager.getId();
    }

    /*
      Valid test for endpoint POST create account
      @author: Maria Klinski
     */
    @Test
    @Order(0)
    public void testCreateValidAccount() {
        // Arrange
        AccountCreationDto body = new AccountCreationDto("Bob", "bob@gmail.com", "Bob123#", "123 Peel St, Montreal, QC");

        // Act
        ResponseEntity<AccountResponseDto> response = client.postForEntity("/accounts", body, AccountResponseDto.class);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0, "the ID should be a positive integer");
        this.createdAccountId = response.getBody().getId();
        assertEquals(body.getUsername(), response.getBody().getUsername());
        assertEquals(body.getEmail(), response.getBody().getEmail());
        assertEquals(body.getAddress(), response.getBody().getAddress());

    }

    /*
      Invalid test for endpoint POST create account, duplicate email
      @author: Maria Klinski
     */
    @Test
    @Order(1)
    public void testCreateAccountDuplicateEmail() {
        // Arrange
        AccountCreationDto body = new AccountCreationDto("NotBob", testCustomer.getEmail(), "NotBob123#", "111 Peel St, Montreal, QC");

        // Act
        ResponseEntity<ErrorDto> response = client.postForEntity("/accounts", body, ErrorDto.class);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("An account with the same email already exists."), response.getBody().getErrors());
    }
   
    /*
      Valid test for endpoint GET find account by id
      @author: Maria Klinski
     */
    @Test
    @Order(2)
    public void testFindAccountByValidId() {
        // Arrange
        String url = String.format("/accounts/%d", this.createdAccountId);

        // Act
        ResponseEntity<AccountResponseDto> response = client.getForEntity(url, AccountResponseDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(this.createdAccountId, response.getBody().getId());
        assertEquals(testAccountManager.getUsername(), response.getBody().getUsername());
        assertEquals(testAccountManager.getEmail(), response.getBody().getEmail());
        assertEquals(testAccountManager.getAddress(), response.getBody().getAddress());
    }

    /*
      Invalid test for endpoint GET find account by id, invalid id
      @author: Maria Klinski
     */
    @Test
    @Order(3)
    public void testFindAccountByInvalidId() {
        // Arrange
        int invalidId = -1;
        String url = String.format("/accounts/%d", invalidId);

        // Act
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("No account found with id " + invalidId), response.getBody().getErrors());

    }

    /*
      Valid test for endpoint GET find account by username
      @author: Maria Klinski
     */
    @Test
    @Order(4)
    public void testFindAccountByValidUsername() {
        // Arrange
        String url = String.format("/accounts/username/%s", testAccountManager.getUsername());

        // Act
        ResponseEntity<AccountResponseDto> response = client.getForEntity(url, AccountResponseDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(this.createdAccountId, response.getBody().getId());
        assertEquals(testAccountManager.getUsername(), response.getBody().getUsername());
        assertEquals(testAccountManager.getEmail(), response.getBody().getEmail());
        assertEquals(testAccountManager.getAddress(), response.getBody().getAddress());
    }

    /*
      Invalid test for endpoint GET find account by username, invalid username
      @author: Maria Klinski
     */
    @Test
    @Order(5)
    public void testFindAccountByInvalidUsername() {
        // Arrange
        String invalidUsername = "invalidUsername";
        String url = String.format("/accounts/username/%s", invalidUsername);

        // Act
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("No account found with username " + invalidUsername), response.getBody().getErrors());
    
    }      
    /*
        Valid test for endpoint GET find account by email
        @author: Maria Klinski
     */
    @Test
    @Order(6)
    public void testFindAccountByValidEmail() {
        // Arrange
        String url = String.format("/accounts/email/%s", testAccountManager.getEmail());

        // Act
        ResponseEntity<AccountResponseDto> response = client.getForEntity(url, AccountResponseDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(this.createdAccountId, response.getBody().getId());
        assertEquals(testAccountManager.getUsername(), response.getBody().getUsername());
        assertEquals(testAccountManager.getEmail(), response.getBody().getEmail());
        assertEquals(testAccountManager.getAddress(), response.getBody().getAddress());
    }

    /*
        Invalid test for endpoint GET find account by email, invalid email
        @author: Maria Klinski
     */
    @Test
    @Order(7)
    public void testFindAccountByInvalidEmail() {
        // Arrange
        String invalidEmail = "invalid@something.com";
        String url = String.format("/accounts/email/%s", invalidEmail);

        // Act
        ResponseEntity<ErrorDto> response = client.getForEntity(url, ErrorDto.class);
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("No account found with email " + invalidEmail), response.getBody().getErrors());
    }

    /*
        Valid test for endpoint POST login
        @author: Maria Klinski
     */
    @Test
    @Order(8)
    public void testLoginValid() {

        // Act
        String url = String.format("/accounts/login?email=%s&password=%s", testAccountManager.getEmail(), testAccountManager.getPassword());
        ResponseEntity<AccountResponseDto> response = client.postForEntity(url, null, AccountResponseDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(this.createdAccountId, response.getBody().getId());
        assertEquals(testAccountManager.getUsername(), response.getBody().getUsername());
        assertEquals(testAccountManager.getEmail(), response.getBody().getEmail());
        assertEquals(testAccountManager.getAddress(), response.getBody().getAddress());
    }

    /*
        Invalid test for endpoint POST login, invalid password
        @author: Maria Klinski
     */
    @Test
    @Order(9)
    public void testLoginInvalidPassword() {
        // Arrange
        String invalidPassword = "WrongPass!";

        // Act
        String url = String.format("/accounts/login?email=%s&password=%s", testAccountManager.getEmail(), invalidPassword);
        ResponseEntity<ErrorDto> response = client.postForEntity(url, null, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("Invalid password."), response.getBody().getErrors());
    }

    /*
        Valid test for endpoint PUT update account
        @author: Maria Klinski
     */
    @Test
    @Order(10)
    public void testUpdateAccountValid() {
        // Arrange
        String newAddress = "272 Bob St. , Montreal, QC";

        AccountCreationDto body = new AccountCreationDto(testAccountManager.getUsername(), testAccountManager.getEmail(), testAccountManager.getPassword(), newAddress);

        // Act
        ResponseEntity<AccountResponseDto> response = client.exchange("/accounts/" + this.createdAccountId, HttpMethod.PUT, new HttpEntity<>(body), AccountResponseDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(this.createdAccountId, response.getBody().getId());
        assertEquals(testAccountManager.getUsername(), response.getBody().getUsername());
        assertEquals(testAccountManager.getEmail(), response.getBody().getEmail());
        assertEquals(newAddress, response.getBody().getAddress());

    }

    /*
        Valid test for endpoint DELETE delete account
        @author: Maria Klinski
     */
    @Test
    @Order(11)
    public void testDeleteAccountValid() {
        // Arrange
        String url = String.format("/accounts/%d", this.createdAccountId);

        // Act
        ResponseEntity<Void> response = client.exchange(url, HttpMethod.DELETE, null, Void.class);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
}
}