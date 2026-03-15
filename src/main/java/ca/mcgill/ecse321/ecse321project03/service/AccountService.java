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


    //------------------------
    // Related to Loyalty Points
    //------------------------

    /**
     * Awards Loyalty points
     * @param accountId id of the user receiving the loyalty points
     * @param orderId the ID of the order that triggers the loyalty-point award
     * @author Emily Sun
     */
    @Transactional
    public Account awardLoyaltyPts(int accountId, int orderId) {
        Account accountFromDB = findAccountById(accountId);
        Order orderFromDB = orderRepository.findById(orderId).orElse(null);

        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No order found with id " + orderId);
        }

        if (orderFromDB.getCustomer() == null || orderFromDB.getCustomer().getId() != accountId) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN,
                    "This order does not belong to the account with id " + accountId + ".");
        }

        List<Order> orderList = orderRepository.findByCustomer_Id(accountId);
        if(orderList.size() > 1){
           addLoyaltyPtsForOrder(accountFromDB);
        }
        return accountRepository.save(accountFromDB);
    }


    public void addLoyaltyPtsForOrder(Account accountFromDB){
        int pts = accountFromDB.getLoyaltyPts();
        pts += 1;
        accountFromDB.setLoyaltyPts(pts);
    }

    /**
     * Redeem your loyalty points
     * @param accountId id of the user
     * @param orderId id of the order for which you want to redeem the points
     * @author Emily Sun and Sonia Ly
     */
    @Transactional
    public String redeemLoyaltyPts (int accountId, int orderId, boolean usePts){
        Account accountFromDB = findAccountById(accountId);
        Order orderFromDB = orderRepository.findById(orderId).orElse(null);

        if (accountFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + accountId + " found.");
        }
        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No order with id " + orderId + " found.");
        }
        if (orderFromDB.getCustomer() == null || orderFromDB.getCustomer().getId() != accountId) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN,
                    "This order does not belong to the account with id " + accountId + ".");
        }

        double price = orderService.findOrderPrice(orderFromDB);
        double moneySaved = useLoyaltyPoints(accountFromDB) * price;
        if(moneySaved == 0.0){
            return "";
        }
        return "You saved " + String.format("%.2f", moneySaved) + " on this order!";

    }

    /**
     * Use up loyalty points
     * @param account account that's using the loyalty points
     * @author Emily Sun
     */
    public double useLoyaltyPoints(Account account){
        int lytPts = account.getLoyaltyPts();
        if(lytPts < 10){
            //no discount
            return 0.0;
        }
        else if (lytPts < 15){
            // 10-14 pts -> 10% off
            return 0.1;
        }
        else if (lytPts < 20){
            //15-19 pts -> 20% off
            return 0.2;
        }
        else if (lytPts < 25){
            //20-24 pts -> 30% off
            return 0.3;
        }
        else if (lytPts < 35){
            //25-34 pts -> 40% off
            return 0.4;
        }else{
            // lytpts >= 35 -> 50% off (caps off at 50%)
            return 0.5;
        }
    }

}
