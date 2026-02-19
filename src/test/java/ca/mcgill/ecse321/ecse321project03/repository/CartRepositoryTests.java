package ca.mcgill.ecse321.ecse321project03.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Cart;

@SpringBootTest
public class CartRepositoryTests {

    
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach

    @AfterEach
    public void clearDatabase() {
        cartRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void testCreateAndReadCart() {
        String username = "Joyanne Smith";
        String email = "joyanne.smith@gmail.com";
        String password = "Password127!";
        String address = "123 Univeristy Street, Montreal, QC";
        int loyaltyPts = 100;
        Account account = new Account(username, email, password, address, loyaltyPts);

        accountRepository.save(account);

        Cart cart = new Cart(account);
        cartRepository.save(cart);

        int cartId = cart.getId();
        Cart retrievedCart = cartRepository.findById(cartId).orElse(null);

        assertNotNull(retrievedCart);
        assertEquals(account.getId(), retrievedCart.getAccount().getId());
        assertEquals(cartId, retrievedCart.getId());
        assertNotNull(account.getId()); retrievedCart.getAccount().getId();
    }
    
}
