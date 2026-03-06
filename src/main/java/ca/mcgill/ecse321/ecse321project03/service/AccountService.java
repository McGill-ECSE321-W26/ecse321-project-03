package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
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
    private OrderService orderService;

    @Transactional
    public Account createAccount(@Valid AccountCreationDto accountCreationDto) {
        
        Account account = new Account(accountCreationDto.getUsername(), accountCreationDto.getPassword(), accountCreationDto.getEmail(), accountCreationDto.getAddress(), 0);

        // Save the account to the database
        return accountRepository.save(account);
    }

    public Account findAccountById (int id) {
        Account accountFromDB = accountRepository.findById(id).orElse(null);
        if (accountFromDB == null) {
             throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person found with id " + id);
        }
        return accountFromDB;
    }

    public Account awardLoyaltyPts(int accountID){
        //using the find method from above
        Account accountFromDB = findAccountById(accountID);
        //find order list for account
        List<Order> orderList = orderRepository.findByCustomer_Id(accountID);
        //check if customer is returning customer
        if(!orderList.isEmpty()){
            //if so, award points
            addLoyaltyPtsForOrder(accountFromDB);
        }
        return accountFromDB;
    }

    public void addLoyaltyPtsForOrder(Account accountFromDB ){
        int pts = accountFromDB.getLoyaltyPts();
        pts += 1;
        accountFromDB.setLoyaltyPts(pts);
    }

    public Account redeemLoyaltyPtsInOrder(int accountID, Order order, int pointsUsed){
        Account accountFromDB = findAccountById(accountID);
        double orderPrice = orderService.findOrderPrice(order);
        //round up the int -> if order price is $67.67
        //since 1 point = $1 off, then you would need a full point to cover the $0.67 extra
        //that way, when they use 68 points, the final price will be $0
        int intPrice = (int) Math.ceil(orderPrice);
        //use smallest out of price and pointsUsed because final price cannot be negative
        if(accountFromDB.getLoyaltyPts() == 0){
            //i think we could go without the logic since i can deal with it in the helper function
            //but i wanted the functionality to be that if they didn't have loyalty points, we dont even call the use function
            //but idk how to implement that teehee :P
            return accountFromDB;
        }
        useLoyaltyPoints(accountFromDB, Math.min(pointsUsed, intPrice));
        return accountFromDB;
    }

    public void useLoyaltyPoints(Account account, int pts){
        int loyaltyPts = account.getLoyaltyPts();
        if(loyaltyPts <= pts){
            //if the number of points they want to use is greater or equal to the number they have
            //just use all the points
            account.setLoyaltyPts(0);
        }
        else{
            account.setLoyaltyPts(loyaltyPts - pts);
        }
    }

}
