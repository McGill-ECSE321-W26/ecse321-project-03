package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import org.hibernate.annotations.processing.Find;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
import jakarta.transaction.Transactional;
import ca.mcgill.ecse321.ecse321project03.model.Account;

import java.util.List;


// Tutorial 7
@Service
@Validated
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderService orderService;

    /**
     * Create an account based on the AccountCreationDTO transfer object
     *
     * @param accountCreationDto The transfer object created
     * @return the account
     * @author Maria Klinski
     */
    @Transactional
    public Account createAccount(@Valid AccountCreationDto accountCreationDto) {
        
        // check if account already exists with the same email
        if (accountRepository.findByEmail(accountCreationDto.getEmail()) != null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "An account with email " + accountCreationDto.getEmail() + " already exists");
        }

        // check if account already exists with the same username
        if (accountRepository.findByUsername(accountCreationDto.getUsername()) != null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "An account with username " + accountCreationDto.getUsername() + " already exists");
        }

        Account account = new Account(accountCreationDto.getUsername(), accountCreationDto.getEmail(), accountCreationDto.getPassword(), accountCreationDto.getAddress(), 0);

        // Save the account to the database
        return accountRepository.save(account);
    }

    
    /**
     * Find an account corresponding to a given id
     *
     * @param id The id of the account to find
     * @return the account
     * @author Maria Klinski
     */
    public Account findAccountById (int id) {
        Account accountFromDB = accountRepository.findById(id).orElse(null);
        if (accountFromDB == null) {
             throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person found with id " + id);
        }
        return accountFromDB;
    }
/**
     * Find an account corresponding to a given username
     *
     * @param username The username of the account to find
     * @return the account
     * @author Maria Klinski
     */
    public Account findAccountByUsername (String username) {
        Account accountFromDB = accountRepository.findByUsername(username);
        if (accountFromDB == null) {
             throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with username " + username);
        }
        return accountFromDB;
    }

    /**
     * Find an account corresponding to a given email
     *
     * @param email The email of the account to find
     * @return the account
     * @author Maria Klinski
     */
    public Account findAccountByEmail (String email) {
        Account accountFromDB = accountRepository.findByEmail(email);
        if (accountFromDB == null) {
             throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with email " + email);
        }
        return accountFromDB;
    }

    /**
     * Login page
     *
     * @param email The email of the account to find
     * @param password The password of the account to find
     * @return the account
     * @author Maria Klinski
     */
    public Account login (String email, String password) {
        Account accountFromDB = accountRepository.findByEmail(email);
        
        if (accountFromDB == null) {
             throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with email " + email);
        }
        if (!accountFromDB.getPassword().equals(password)) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Incorrect password for email " + email);
        }
        return accountFromDB;
    }

    /**
     * Delete an account
     *
     * @param id The id of the account to delete
     * @author Maria Klinski
     */
    @Transactional
    public void deleteAccount (int id) {
        Account accountFromDB = accountRepository.findById(id).orElse(null);
        
        // check if account exists
        if (accountFromDB == null) {
             throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with id " + id);
        }

        Role role = roleRepository.findByAccount_Id(id);
        // check if account is a manager account
        if (role instanceof Manager) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Cannot delete a manager account");
        }
        accountRepository.delete(accountFromDB);
    }

    /**
     * Update account
     *
     * @param id The id of the account to update
     * @param accountCreationDto The updated account information
     * @return the updated account
     * @author Maria Klinski
     */

    @Transactional
    public Account updateAccount (int id, @Valid AccountCreationDto accountCreationDto) {
        Account accountFromDB = accountRepository.findById(id).orElse(null);
        
        // check if account exists
        if (accountFromDB == null) {
             throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with id " + id);
        }

        // check if account already exists with the same email
        Account accountWithSameEmail = accountRepository.findByEmail(accountCreationDto.getEmail());
        if (accountWithSameEmail != null && accountWithSameEmail.getId() != id) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "An account with email " + accountCreationDto.getEmail() + " already exists");
        }

        // check if account already exists with the same username
        Account accountWithSameUsername = accountRepository.findByUsername(accountCreationDto.getUsername());
        if (accountWithSameUsername != null && accountWithSameUsername.getId() != id) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "An account with username " + accountCreationDto.getUsername() + " already exists");
        }

        // Update the fields of the existing account
        accountFromDB.setUsername(accountCreationDto.getUsername());
        accountFromDB.setPassword(accountCreationDto.getPassword());
        accountFromDB.setEmail(accountCreationDto.getEmail());
        accountFromDB.setAddress(accountCreationDto.getAddress());

        // save the updated account into the database
        return accountRepository.save(accountFromDB);
    }




}
