package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.dto.CartItemDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemTypeDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Cart;
import ca.mcgill.ecse321.ecse321project03.model.CartItem;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;


    /**
     * Associates a new cart with a user
     *
     * @param id of the user
     * @author Sonia Ly
     */
    @Transactional
    public Cart createNewCart(int id) {
        // Check if the user already as a cart (a user should only have one cart at the time)
        Account customer = accountRepository.findById(id).orElse(null);
        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + id + " found.");
        }

        Cart customerCart = cartRepository.findByAccount_Id(id);
        if (customerCart != null) {
            throw new Ecse321Project03Exception(HttpStatus.CONFLICT,
                    "A cart already exists! Users may only have one active cart at the time.");
        }

        // Create a new cart
        Cart cart = new Cart(customer);
        return cartRepository.save(cart);
    }

    /**
     * Deletes a user's cart if it exists.
     *
     * @param id of the user
     * @author Sonia Ly
     */
    @Transactional
    public void deleteCart(int id) {

        // Check that the user has a cart to delete
        Account customer = accountRepository.findById(id).orElse(null);
        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + id + " found.");
        }

        // See if the user already has a cart
        Cart customerCart = cartRepository.findByAccount_Id(id);
        if (customerCart == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "Impossible to delete the cart. No carts are associate to this customer.");
        }

        // Delete all cartItem before delete the cart
        // Otherwise, the cartItems are still in the database with a not existing cart
        List<CartItem> cartItems = cartItemRepository.findByCartItemId_Cart(customerCart);
        if (cartItems != null && !cartItems.isEmpty()) {
            cartItemRepository.deleteAll(cartItems);
        }

        cartRepository.delete(customerCart);
    }

    /**
     * Adds an item to the customer's cart. If the item already exists in the cart,
     * its quantity is increased.
     *
     * @param id the user's id
     * @param cartItemDto the cart item data containing the item type and quantity
     * @return the saved CartItem entity
     */
    @Transactional
    public CartItem addItemToCart(int id, CartItemDto cartItemDto) {

        Account customer = accountRepository.findById(id).orElse(null);
        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No account with id " + id + " found.");
        }

        Cart customerCart = cartRepository.findByAccount_Id(id);
        if (customerCart == null) {
            throw new Ecse321Project03Exception(HttpStatus.CONFLICT,
                    "Cannot add an item to the cart. This customer doesn't have a cart!");
        }

        // Check whether CartItemDto is valid
        if (cartItemDto == null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                    "Cart item data must not be null.");
        }
        if (cartItemDto.getItemType() == null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                    "Item type must not be null.");
        }
        if (cartItemDto.getQuantity() <= 0) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                    "Quantity must be positive.");
        }

        ItemTypeDto itemTypeDto = cartItemDto.getItemType();
        int itemTypeId =  itemTypeDto.getId();
        ItemType itemType = itemTypeRepository.findById(itemTypeId).orElse(null);
        if (itemType == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No item type found with id " + itemTypeId + ".");
        }
        if (cartItemDto.getQuantity() > itemType.getStock()) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                    "Requested quantity exceeds available stock.");
        }

        CartItem.CartItemId cartItemId = new CartItem.CartItemId(itemType, customerCart);
        CartItem existingCartItem = cartItemRepository.findById(cartItemId).orElse(null);

        // If cartItem already exist, just add the quantity
        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItemDto.getQuantity());
            return cartItemRepository.save(existingCartItem);
        }
        // Create a new CartItem with corresponding quantity
        CartItem newCartItem = new CartItem(cartItemId, cartItemDto.getQuantity());
        return cartItemRepository.save(newCartItem);
    }

    /**
     * Calculates the total price of all items currently in a user's cart.
     *
     * @param id the id of the user
     * @return the total price of the cart
     * @author Sonia Ly
     */
    @Transactional
    public double findCartTotalPrice(int id) {
        double total = 0;

        // Check if the user already as a cart (a user should only have one cart at the time)
        Account customer = accountRepository.findById(id).orElse(null);
        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + id + " found.");
        }

        // See if the user already has a cart
        Cart customerCart = cartRepository.findByAccount_Id(id);
        if (customerCart == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "This user's cart does not exist. Cannot calculate the cart total.");
        }

        List<CartItem> cartItems = cartItemRepository.findByCartItemId_Cart(customerCart);

        for (CartItem cartItem : cartItems) {
            ItemType itemType = cartItem.getCartItemId().getItemType();
            double price = itemType.getGeneralItem().getPrice();
            int quantity = cartItem.getQuantity();
            total += price * quantity;
        }

        return total;
    }

}
