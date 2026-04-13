package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.dto.AccountResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;
import jakarta.transaction.Transactional;
import ca.mcgill.ecse321.ecse321project03.model.Account;

import java.util.ArrayList;
import java.util.List;

// Tutorial 7
@Service
@Validated
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ManagerRepository managerRepository;

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

        if ("MANAGER".equalsIgnoreCase(accountCreationDto.getAccountType().toString())) {
        // CrudRepository gives you count() for free!
        if (this.managerRepository.count() > 0) {
            throw new Ecse321Project03Exception(HttpStatus.CONFLICT, "A Manager account already exists. Only one manager is allowed in the system.");
        }
    }

        Account account = new Account(accountCreationDto.getUsername(), accountCreationDto.getEmail(), accountCreationDto.getPassword(), accountCreationDto.getAddress(), 0);

        Account saved = accountRepository.save(account);
        assignRoleForNewAccount(saved, accountCreationDto.getAccountType());
        return saved;
    }

    /**
     * Builds a response DTO including {@link AccountResponseDto#getAccountType()} from persisted role data.
     */
    public AccountResponseDto toAccountResponseDto(Account account) {
        Role role = roleRepository.findByAccount_Id(account.getId());
        return new AccountResponseDto(account, role);
    }

    private void assignRoleForNewAccount(Account account, String accountTypeRaw) {
        String type = normalizeAccountType(accountTypeRaw);
        switch (type) {
            case "MANAGER" -> roleRepository.save(new Manager(account));
            case "EMPLOYEE" -> roleRepository.save(new Employee(account));
            default -> { /* CUSTOMER: no role row */ }
        }
    }

    private static String normalizeAccountType(String raw) {
        if (raw == null || raw.isBlank()) {
            return "CUSTOMER";
        }
        String u = raw.trim().toUpperCase();
        if ("MANAGER".equals(u) || "EMPLOYEE".equals(u) || "CUSTOMER".equals(u)) {
            return u;
        }
        throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                "accountType must be MANAGER, EMPLOYEE, or CUSTOMER.");
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

        if (!accountFromDB.getActive()) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "This account has been deactivated: " + email);
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
    public void deleteAccount(int id) {
    Account accountFromDB = accountRepository.findById(id).orElse(null);

    if (accountFromDB == null) {
        throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with id " + id);
    }

    Role role = roleRepository.findByAccount_Id(id);
    if (role instanceof Manager) {
        throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Cannot delete a manager account");
    }

    accountFromDB.setActive(false);
    accountRepository.save(accountFromDB);
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

    public int getLoyaltyPoints(int accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No account found with id " + accountId);
        }
        return account.getLoyaltyPts();
    }

    public Account updateAddress(int id, String address) {
        Account account = findAccountById(id);
        account.setAddress(address);
        return accountRepository.save(account);
    }

    /**
     * Author: Sonia Ly
     * @return a list of all accounts
     */
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = (List<Account>) accountRepository.findAll();
        List<AccountResponseDto> allAccountsDTO = new ArrayList<>();

        for (Account account : accounts) {
            if (account.getActive()) {
                allAccountsDTO.add(toAccountResponseDto(account));
            }
        }

        return allAccountsDTO;
    }
}
