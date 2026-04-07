package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.AccountResponseDto;
import ca.mcgill.ecse321.ecse321project03.dto.AddressUpdateDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for handling all account-related API endpoints.
 * It connects HTTP requests from the client to the AccountService layer.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Creates a new account in the system.
     *
     * @param accountCreationDto the DTO containing account information such as username, email, password, and address
     * @return a ResponseEntity containing the created AccountResponseDto or an ErrorDto if the creation fails
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody AccountCreationDto accountCreationDto) {
        try {
            Account account = accountService.createAccount(accountCreationDto);
            return new ResponseEntity<>(accountService.toAccountResponseDto(account), HttpStatus.CREATED);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves an account by its unique ID.
     *
     * @param id the ID of the account
     * @return a ResponseEntity containing the AccountResponseDto or an ErrorDto if the account is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findAccountById(@PathVariable int id) {
        try {
            Account account = accountService.findAccountById(id);
            return new ResponseEntity<>(accountService.toAccountResponseDto(account), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves an account using its username.
     *
     * @param username the username of the account to retrieve
     * @return a ResponseEntity containing the AccountResponseDto if the account exists,
     *         or an ErrorDto if the account cannot be found
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> findAccountByUsername(@PathVariable String username) {
        try {
            Account account = accountService.findAccountByUsername(username);
            return new ResponseEntity<>(accountService.toAccountResponseDto(account), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves an account using its email address.
     *
     * @param email the email address of the account to retrieve
     * @return a ResponseEntity containing the AccountResponseDto if the account exists,
     *         or an ErrorDto if the account cannot be found
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> findAccountByEmail(@PathVariable String email) {
        try {
            Account account = accountService.findAccountByEmail(email);
            return new ResponseEntity<>(accountService.toAccountResponseDto(account), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Authenticates an account using email and password.
     *
     * @param email the email of the account
     * @param password the password of the account
     * @return a ResponseEntity containing the AccountResponseDto if authentication succeeds, or an ErrorDto if it fails
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            Account account = accountService.login(email, password);
            return new ResponseEntity<>(accountService.toAccountResponseDto(account), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Updates the information of an existing account.
     *
     * @param id the ID of the account to update
     * @param accountCreationDto the DTO containing the updated account information
     * @return a ResponseEntity containing the updated AccountResponseDto if successful,
     *         or an ErrorDto if the update fails
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable int id, @RequestBody AccountCreationDto accountCreationDto) {
        try {
            Account account = accountService.updateAccount(id, accountCreationDto);
            return new ResponseEntity<>(accountService.toAccountResponseDto(account), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes an account from the system.
     *
     * @param id the ID of the account to delete
     * @return a ResponseEntity with NO_CONTENT status if deletion succeeds,
     *         or an ErrorDto if the account cannot be found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable int id) {
        try {
            accountService.deleteAccount(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/address")
    public AccountResponseDto updateAddress(@PathVariable int id, @RequestBody AddressUpdateDto updateDto) {
        Account account = accountService.updateAddress(id, updateDto.getAddress());
        return accountService.toAccountResponseDto(account);
    }

    @GetMapping("/{id}/loyalty-points")
    public int getLoyaltyPoints(@PathVariable int id) {
        return accountService.getLoyaltyPoints(id);
    }
}
