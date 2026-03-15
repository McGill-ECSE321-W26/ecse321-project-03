package ca.mcgill.ecse321.ecse321project03.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;

import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@SpringBootTest
public class AccountServiceTests {
    @Mock
    private AccountRepository repo;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private AccountService accountService;

    private String username;
    private String email;
    private String password;
    private String address;
    private int loyaltyPts;
    private int id;
    private AccountCreationDto dto;
    private Account account;
    private AccountCreationDto newAccountCreationDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        username = "user1";
        email = "email1@mail.com";
        password = "p";
        address = "123 Street";
        loyaltyPts = 0;
        id = 5;

        dto = new AccountCreationDto(username, email, password, address);
        newAccountCreationDto = new AccountCreationDto("NewUser", "newEmail@mail.com", "NewPassword", "new Street");
        account = new Account(username, email, password, address, loyaltyPts);
    }

    // ---------- createAccount: success  ----------
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

    // ---------- createAccount: failures  ----------
    @Test
    public void testCreateAccountDuplicateEmail() {
        Account accountDuplicate = new Account("user2", email, "password", "456 Street", 1);
        when(repo.findByEmail(email)).thenReturn(accountDuplicate);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.createAccount(dto));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("An account with email " + email + " already exists", e.getMessage());
    }

    @Test
    public void testCreateAccountDuplicateUsername() {
        Account accountDuplicate = new Account(username, "email2@mail.com", "password", "456 Street", 1);
        when(repo.findByUsername(username)).thenReturn(accountDuplicate);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.createAccount(dto));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("An account with username " + username + " already exists", e.getMessage());
    }

    // ---------- findAccountById : success  ----------
    @Test
    public void testFindAccountByValidId() {
        when(repo.findById(id)).thenReturn(Optional.of(account));

        Account foundAccount = accountService.findAccountById(id);

        assertNotNull(foundAccount);
        assertEquals(foundAccount.getUsername(), username);
        assertEquals(foundAccount.getPassword(), password);
        assertEquals(foundAccount.getEmail(), email);
        assertEquals(foundAccount.getAddress(), address);
        assertEquals(foundAccount.getLoyaltyPts(), loyaltyPts);
    }

    // ---------- findAccountById : failure  ----------
    @Test
    public void testFindAccountByInvalidId() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.findAccountById(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No person found with id " + id, e.getMessage());
    }

    // ---------- findAccountByUsername : success  ----------
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

    // ---------- findAccountByUsername : failure  ----------
    @Test
    public void testFindAccountByInValidUsername() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.findAccountByUsername(username));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with username " + username, e.getMessage());
    }

    // ---------- findAccountByEmail : success  ----------
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

    // ---------- findAccountByEmail : failure  ----------
    @Test
    public void testFindAccountByInValidEmail() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.findAccountByEmail(email));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with email " + email, e.getMessage());
    }

    // ---------- login : success  ----------
    @Test
    public void testLoginValidCredentials() {
        when(repo.findByEmail(email)).thenReturn(account);

        Account loggedInAccount = accountService.login(email, password);

        assertNotNull(loggedInAccount);
        assertEquals(loggedInAccount.getUsername(), username);
        assertEquals(loggedInAccount.getPassword(), password);
        assertEquals(loggedInAccount.getEmail(), email);
        assertEquals(loggedInAccount.getAddress(), address);
        assertEquals(loggedInAccount.getLoyaltyPts(), loyaltyPts);
    }

    // ---------- login : failures  ----------
    @Test
    public void testLoginInvalidEmail() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.login(email, password));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with email " + email, e.getMessage());
    }

    @Test
    public void testLoginInvalidPassword() {
        String invalidPassword = "wrongPassword";
        when(repo.findByEmail(email)).thenReturn(account);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.login(email, invalidPassword));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Incorrect password for email " + email, e.getMessage());
    }

    // ---------- deleteAccount : success  ----------
    @Test
    public void testDeleteAccountSuccessful(){
        Role nonManagerRole = new Employee(account);
        when(repo.findById(id)).thenReturn(Optional.of(account));
        when(roleRepository.findByAccount_Id(id)).thenReturn(nonManagerRole);

        assertDoesNotThrow(() -> accountService.deleteAccount(id));

        verify(repo, times(1)).delete(account);
    }

    // ---------- deleteAccount : failures  ----------
    @Test
    public void testDeleteAccountInvalidId(){
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.deleteAccount(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with id " + id, e.getMessage());
    }

    @Test
    public void testDeleteAccountManagerRole(){
        Role managerRole = new Manager(account);
        when(repo.findById(id)).thenReturn(Optional.of(account));
        when(roleRepository.findByAccount_Id(id)).thenReturn(managerRole);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.deleteAccount(id));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Cannot delete a manager account", e.getMessage());
    }

    // ---------- updateAccount : success  ----------
    @Test
    public void testUpdateAccountValidInputs(){
        when(repo.findById(id)).thenReturn(Optional.of(account));
        when(repo.save(any(Account.class))).thenReturn(account);

        Account savedAccount = accountService.updateAccount(id, newAccountCreationDto);

        assertNotNull(savedAccount);
        assertEquals("NewUser", savedAccount.getUsername());
        assertEquals("newEmail@mail.com", savedAccount.getEmail());
        assertEquals("NewPassword", savedAccount.getPassword());
        assertEquals("new Street", savedAccount.getAddress());
        assertEquals(loyaltyPts, savedAccount.getLoyaltyPts());

        verify(repo, times(1)).save(any(Account.class));
    }

    // ---------- updateAccount : failures  ----------
    @Test
    public void testUpdateAccountNotFound(){
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.updateAccount(id, newAccountCreationDto));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with id " + id, e.getMessage());
    }

    @Test
    public void testUpdateAccountInvalidEmail(){
        Account existingAccount = mock(Account.class);
        when(existingAccount.getId()).thenReturn(6);
        when(repo.findById(id)).thenReturn(Optional.of(account));
        when(repo.findByEmail(newAccountCreationDto.getEmail())).thenReturn(existingAccount);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.updateAccount(id, newAccountCreationDto));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("An account with email " + newAccountCreationDto.getEmail() + " already exists", e.getMessage());
    }

    @Test
    public void testUpdateAccountInvalidUsername(){
        Account existingAccount = mock(Account.class);
        when(existingAccount.getId()).thenReturn(6);
        when(repo.findById(id)).thenReturn(Optional.of(account));
        when(repo.findByUsername(newAccountCreationDto.getUsername())).thenReturn(existingAccount);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> accountService.updateAccount(id, newAccountCreationDto));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("An account with username " + newAccountCreationDto.getUsername() + " already exists", e.getMessage());
    }
}
