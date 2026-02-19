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
import ca.mcgill.ecse321.ecse321project03.model.CartItem;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;

@SpringBootTest
public class CartItemRepositoryTests {

    
    @Autowired
    private CartItemRepository cartItemRepository;

   

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

     @Autowired
    private ItemRepository itemRepository;

    @BeforeEach

    @AfterEach
    public void clearDatabase() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        accountRepository.deleteAll();
        itemTypeRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    public void testCreateAndReadCartItem() {
        Item item = new Item("Sunflower", Item.Tag.Dress, 59.99);
        itemRepository.save(item);
        
        ItemType itemType = new ItemType(ItemType.Size.XS, ItemType.Color.White, 2, item);
        itemTypeRepository.save(itemType);

        String username = "Joyanne Smith";
        String email = "joyanne.smith@gmail.com";
        String password = "Password127!";
        String address = "123 Univeristy Street, Montreal, QC";
        int loyaltyPts = 100;
        Account account = new Account(username, email, password, address, loyaltyPts);

        accountRepository.save(account);

        Cart cart = new Cart(account);
        cartRepository.save(cart);

        CartItem.CartItemId cartItemId = new CartItem.CartItemId(itemType, cart, 3);
        CartItem cartItem = new CartItem(cartItemId);
        cartItemRepository.save(cartItem);
        
         CartItem retrievedCartItem = cartItemRepository.findById(cartItemId).orElse(null);

        assertNotNull(retrievedCartItem);
        assertNotNull(retrievedCartItem.getCartItemId().getCart());
        assertNotNull(retrievedCartItem.getCartItemId().getItemType());
        assertEquals(3, retrievedCartItem.getCartItemId().getQuantity());
        assertEquals(cart.getId(), retrievedCartItem.getCartItemId().getCart().getId());
        assertEquals(itemType.getId(), retrievedCartItem.getCartItemId().getItemType().getId());
    }
    
}
