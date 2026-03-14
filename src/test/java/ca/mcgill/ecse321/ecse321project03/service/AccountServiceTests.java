package ca.mcgill.ecse321.ecse321project03.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)  // replaces @SpringBootTest
public class AccountServiceTests {
    @Mock
    private AccountRepository repo;
    @InjectMocks
    private AccountService accountService;

    private String username;
    private String email;
    private String password;
    private String address;
    private int loyaltyPts;
    private AccountCreationDto dto;
    private Account account;

    @BeforeEach
    void setup() {
        username = "user1";
        email = "email1@mail.com";
        password = "p";
        address = "123 Street";
        loyaltyPts = 0;

        dto = new AccountCreationDto(username, email, password, address);
        account = new Account(username, email, password, address, loyaltyPts);
    }

    @Test
    public void testCreateValidAccount() {
        when(repo.save(any(Account.class))).thenReturn(account);

        Account accountCreated = accountService.createAccount(dto);

        assertNotNull(accountCreated);
        assertEquals(accountCreated.getUsername(), username);
        assertEquals(accountCreated.getEmail(), email);
        assertEquals(accountCreated.getPassword(), password);
        assertEquals(accountCreated.getAddress(), address);
        assertEquals(accountCreated.getLoyaltyPts(), 0);

        verify(repo, times(1)).save(any(Account.class));
    }

    @Test
    public void testCreateAccountDuplicateEmail() {
        //Arrange
        Account accountDuplicate = new Account("user2", email, "password", "456 Street", 1);
        when(repo.findByEmail(email)).thenReturn(accountDuplicate);

        // Act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.createAccount(dto));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("An account with email " + email + " already exists", e.getMessage());
    }

    @Test
    public void testCreateAccountDuplicateUsername() {
        //Arrange
        Account accountDuplicate = new Account(username, "email2@mail.com", "password", "456 Street", 1);
        when(repo.findByUsername(username)).thenReturn(accountDuplicate);

        // Act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.createAccount(dto));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("An account with username " + username + " already exists", e.getMessage());
    }

    @Nested
    class findAccountByTests {
//        public void testFindAccountByValidId() {
//            //Arrange
//            int id = 5;
//            when(repo.findById(id)).thenReturn(account);
//
//            Account foundAccount = accountService.findAccountById(id);
//
//            assertNotNull(foundAccount);
//            assertEquals(foundAccount.getUsername(), username);
//            assertEquals(foundAccount.getPassword(), password);
//            assertEquals(foundAccount.getEmail(), email);
//            assertEquals(foundAccount.getAddress(), address);
//            assertEquals(foundAccount.getLoyaltyPts(), loyaltyPts);
//        }
        @Test
        public void testFindAccountByInvalidId() {
            //Arrange
            int id = 5;

            Ecse321Project03Exception e =
                    assertThrows(Ecse321Project03Exception.class,
                            () -> accountService.findAccountById(id));

            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
            assertEquals("No person found with id " + id, e.getMessage());
        }

        @Test
        public void testFindAccountByValidUsername() {
            when(repo.findByUsername(username)).thenReturn(account);

            Account foundAccount = accountService.findAccountByUsername(username);

            assertNotNull(foundAccount);
            assertEquals(foundAccount.getUsername(), username);
            assertEquals(foundAccount.getPassword(), password);
            assertEquals(foundAccount.getEmail(), email);
            assertEquals(foundAccount.getAddress(), address);
            assertEquals(foundAccount.getLoyaltyPts(), loyaltyPts);
        }

        @Test
        public void testFindAccountByInValidUsername() {
            Ecse321Project03Exception e =
                    assertThrows(Ecse321Project03Exception.class,
                            () -> accountService.findAccountByUsername(username));

            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
            assertEquals("No account found with username " + username, e.getMessage());
        }

        @Test
        public void testFindAccountByValidEmail() {
            when(repo.findByEmail(email)).thenReturn(account);

            Account foundAccount = accountService.findAccountByEmail(email);

            assertNotNull(foundAccount);
            assertEquals(foundAccount.getUsername(), username);
            assertEquals(foundAccount.getPassword(), password);
            assertEquals(foundAccount.getEmail(), email);
            assertEquals(foundAccount.getAddress(), address);
            assertEquals(foundAccount.getLoyaltyPts(), loyaltyPts);
        }

        @Test
        public void testFindAccountByInValidEmail() {
            Ecse321Project03Exception e =
                    assertThrows(Ecse321Project03Exception.class,
                            () -> accountService.findAccountByEmail(email));

            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
            assertEquals("No account found with email " + email, e.getMessage());
        }
    }

    @Test
    public void loginValidCredentials() {
        when(repo.findByEmail(email)).thenReturn(account);

        Account loggedInAccount = accountService.login(email, password);

        assertNotNull(loggedInAccount);
        assertEquals(loggedInAccount.getUsername(), username);
        assertEquals(loggedInAccount.getPassword(), password);
        assertEquals(loggedInAccount.getEmail(), email);
        assertEquals(loggedInAccount.getAddress(), address);
        assertEquals(loggedInAccount.getLoyaltyPts(), loyaltyPts);
    }

    @Test
    public void loginInvalidEmail() {
        String invalidEmail = "invalid@email.com";

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.login(invalidEmail, password));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with email " + email, e.getMessage());
    }

    @Test
    public void loginInvalidPassword() {
        String invalidPassword = "wrongPassword";
        when(repo.findByEmail(email)).thenReturn(account);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.login(email, invalidPassword));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Incorrect password for email " + email, e.getMessage());
    }




}
