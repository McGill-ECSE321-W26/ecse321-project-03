package ca.mcgill.ecse321.ecse321project03.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

//import ca.mcgill.ecse321.eventregistration.exception.EventRegistrationException;
//import ca.mcgill.ecse321.eventregistration.model.Person;
//import ca.mcgill.ecse321.eventregistration.repository.PersonRepository;

@SpringBootTest
public class AccountServiceTests {
    @Mock
    private AccountRepository repo;
    @InjectMocks
    private AccountService accountService;

    private String username;
    private String email;
    private String password;
    private String address;
    private AccountCreationDto dto;

    @BeforeEach
    void setup() {
        username = "user1";
        email = "email1@mail.com"
        password = "p";
        address = "123 Street";
        loyaltyPts = 0;
        dto = new AccounCreationDto(username, email, password, address);

    }

    @Test
    public void testCreateValidAccount() {
        //Arrange
        Account account = new Account(username, password, email, address, loyaltyPts, Date.valueOf(LocalDate.now()));
        when(repo.save(any(Account.class))).thenReturn(account);

        //Act
        Account accountCreated = accountService.createAccount(dto);

        //Assert
        assertNotNull(accountCreated);
        assertEquals(accountCreated.getUsername(), username);
        assertEquals(accountCreat.getEmail(), email);
        assertEquals(accountCreated.getPassword(), password);
        assertEquals(accountCreated.getAddress(), address);
        assertEquals(accountCreated.getLoyaltyPts(), 0);

        assertEquals(Date.valueOf(LocalDate.now(), accountCreated.getCreationDate()));
        verify(repo, times(1)).save(account);
    }

    @Test
    public void testCreateAccountDuplicateEmail() {
        //Arrange
        Account accountDuplicate = new Account("user2", "password", email, "456 Street", 1);
        when(repo.findByEmail(email).thenReturn(accountDuplicate));

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
        Account accountDuplicate = new Account("user1", "password", "email2@mail.com", "456 Street", 1);
        when(repo.findByUsername(username).thenReturn(accountDuplicate));

        // Act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.createAccount(dto));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("An account with username " + username + " already exists", e.getMessage());
    }

    public void testFindAccountByValidId() {
        //Arrange
        int id = 5;
        Date date =
        Account account = new Account(username, password, email, address, loyaltyPts, Date.valueOf(LocalDate.now()));
        when(repo.findById(id)).thenReturn(account);

        Account foundAccount = accountService.findAccountById(id);

        assertNotNull(foundAccount);
        assertEquals(foundAccount.getUsername(), username);
        assertEquals(foundAccount.getPassword(), password);
        assertEquals(foundAccount.getEmail(), email);
        assertEquals(foundAccount.getAddress(), address);
        assertEquals(foundAccount.getCreationDate(), LocalDate.now());
    }
}
