package ca.mcgill.ecse321.ecse321project03.repository;

import ca.mcgill.ecse321.ecse321project03.model.Account;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach

    @AfterEach
    public void clearDatabase() {
        accountRepository.deleteAll();
        cartRepository.deleteAll();
    }

    
    @Test
    public void testCreateAndReadAccount() {
        String username = "Joyanne Smith";
        String email = "joyanne.smith@gmail.com";
        String password = "Password127!";
        String address = "123 Univeristy Street, Montreal, QC";
        int loyaltyPts = 100;
        Account account = new Account(username, email, password, address, loyaltyPts);
        
        accountRepository.save(account);

        int accountId = account.getId();
        Account retrievedAccount = accountRepository.findById(accountId).orElse(null);
        
        assertNotNull(retrievedAccount);
        assertEquals(username, retrievedAccount.getUsername());
        assertEquals(email, retrievedAccount.getEmail());
        assertEquals(address, retrievedAccount.getAddress());
        assertEquals(loyaltyPts, retrievedAccount.getLoyaltyPts());

    }
    


}