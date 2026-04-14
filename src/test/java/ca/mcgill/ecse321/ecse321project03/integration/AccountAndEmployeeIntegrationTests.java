package ca.mcgill.ecse321.ecse321project03.integration;
import ca.mcgill.ecse321.ecse321project03.dto.*;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.repository.EmployeeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

public class AccountAndEmployeeIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    private int createdAccountId;
    private Account testCustomer;
    private Account testAccountEmployee;
    private Account testAccountEmployee2;
    private Account testAccountManager;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private RoleRepository roleRepository;

    @BeforeAll
    @AfterAll
    public void cleanDatabase() {

        employeeRepository.deleteAll();
        managerRepository.deleteAll();
        roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {

        employeeRepository.deleteAll();
        managerRepository.deleteAll();
        roleRepository.deleteAll();
        accountRepository.deleteAll();

        // Customer
        testCustomer = new Account("Jeane", "jeane@gmail.com", "$fjdgbfdju1234", "1000 McGill", 0);
        accountRepository.save(testCustomer);

        // Employee
        testAccountEmployee = new Account("Gabriel", "gabriel@gmail.com", "$fjdgb23525", "2000 McGill", 0);
        accountRepository.save(testAccountEmployee);

        // Employee 2
        testAccountEmployee2 = new Account("Dora", "dora@gmail.com", "$fjdgb23525", "2000 McGill", 0);
        accountRepository.save(testAccountEmployee2);

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
        AccountCreationDto body = new AccountCreationDto("Bob", "bob@gmail.com", "Bob123#456", "123 Peel St, Montreal, QC");
        body.setAccountType("CUSTOMER");
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
        assertIterableEquals(List.of("An account with email " + testCustomer.getEmail() + " already exists"), response.getBody().getErrors());
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
        assertIterableEquals(List.of("No person found with id " + invalidId), response.getBody().getErrors());

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
        assertIterableEquals(List.of("Incorrect password for email " + testAccountManager.getEmail()), response.getBody().getErrors());
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

    /*
        Valid test for endpoint PUT update address
        @author:Wissam Abchiche
     */
    @Test
    public void testUpdateAddressValid() {
        // Arrange
        String url = String.format("/accounts/%d/address", this.createdAccountId);

        String newAddress = "123 new address";

        AddressUpdateDto updateDto = new AddressUpdateDto();
        updateDto.setAddress(newAddress);

        HttpEntity<AddressUpdateDto> request = new HttpEntity<>(updateDto);

        // Act
        ResponseEntity<AccountResponseDto> response =
                client.exchange(url, HttpMethod.PUT, request, AccountResponseDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("123 new address", response.getBody().getAddress());
    }

    /*
      Invalid test for endpoint PUT update address
      @author:Wissam Abchiche
    */
    @Test
    public void testUpdateAddressInvalid() {
        // Arrange
        String url = "/accounts/999999/address";

        AddressUpdateDto updateDto = new AddressUpdateDto();
        updateDto.setAddress("123 new address");

        HttpEntity<AddressUpdateDto> request = new HttpEntity<>(updateDto);

        // Act
        ResponseEntity<ErrorDto> response =
                client.exchange(url, HttpMethod.PUT, request, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("No person found with id " + 999999), response.getBody().getErrors());

    }

    /*
        Valid test for endpoint GET get loyalty points
        @author:Wissam Abchiche
     */
    @Test
    public void testGetLoyaltyPointsValid() {
        // Arrange
        String url = String.format("/accounts/%d/loyalty-points", this.createdAccountId);

        // Act
        ResponseEntity<Integer> response =
                client.exchange(url, HttpMethod.GET, null, Integer.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

    }

    /*
        Invalid test for endpoint GET get loyaltyPoints
        @author: Wissam Abchiche
  */
    @Test
    public void testUGetLoyaltyPointsInvalid() {
        // Arrange
        String url = "/accounts/99999/loyalty-points";

        ResponseEntity<ErrorDto> response =
                client.exchange(url, HttpMethod.GET, null, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("No person found with id " + 99999), response.getBody().getErrors());

    }

    /**
     * Method which validates the get of accounts
     * @author Wissam Abchiche
     */
    @Test
    public void testValidGetAccounts() {


        ResponseEntity<AccountResponseDto[]> response = client.getForEntity(
                "/accounts", AccountResponseDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponseDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(4, responseBody.length);
        assertEquals(testCustomer.getId(), responseBody[0].getId());
        assertEquals(testAccountEmployee.getId(), responseBody[1].getId());
    }

    /**
     * Method which validates the view of employees
     * @author Wissam Abchiche
     */
    @Test
    public void testValidViewEmployees() {
        Employee employee = new Employee(testAccountEmployee);
        Employee employee2 = new Employee(testAccountEmployee2);
        Manager manager = new Manager(testAccountManager);
        employeeRepository.save(employee);
        employeeRepository.save(employee2);
        managerRepository.save(manager);


        ResponseEntity<EmployeeDto[]> response = client.getForEntity(
                "/employees?userID=" + manager.getAccount().getId(), EmployeeDto[].class);


        // Verify status is HTTP.OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EmployeeDto[] responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the correct data is returned
        assertEquals(2, responseBody.length);
        assertEquals(employee.getId(), responseBody[0].getEmployeeId());
        assertEquals(employee2.getId(), responseBody[1].getEmployeeId());
    }


    /**
     * Method which invalidates the view of employees if done by customer
     * @author Wissam Abchiche
     */
    @Test
    public void testInvalidViewEmployees() {

        ResponseEntity<ErrorDto> response = client.getForEntity(
                "/employees?userID=" + testCustomer.getId(), ErrorDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Only manager and employees may view all employees in the system", responseBody.getErrors().get(0));
    }

}