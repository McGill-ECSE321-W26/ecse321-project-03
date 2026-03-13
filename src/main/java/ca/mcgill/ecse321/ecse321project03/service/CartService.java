package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.dto.CartCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Cart;
import ca.mcgill.ecse321.ecse321project03.model.CartItem;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartRepository;
import jakarta.validation.Valid;
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

    private CartRepository cartRepository;
    private AccountRepository accountRepository;

    /**
     * Associate a new cart to a user
     *
     * @param id of the user
     * @author Sonia Ly
     */
    public Cart createNewCart(int id, @Valid CartCreationDto cartCreationDto) {
        // Check if the user already as a cart (a user should only have one cart at the time)
        Account customer = accountRepository.findById(id).orElse(null);

        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + id + " found.");
        } else {
            // See if the user already has a cart
            Cart customerCart = cartRepository.findByAccount_Id(id);
            if (customerCart != null) {
                throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "A cart already exists! Users may only have one active cart at the time.");
            }
        }

        // Create a new cart
        Cart cart = new Cart(cartCreationDto.getAccount());

        return cartRepository.save(cart);
    }

    /**
     * Delete a user's cart (if that cart exists)
     *
     * @param id of the user
     * @author Sonia Ly
     */
    public void deleteCart(int id) {
        // Check that the user has a cart to delete
        Account customer = accountRepository.findById(id).orElse(null);

        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + id + " found.");
        } else {
            // See if the user already has a cart
            Cart customerCart = cartRepository.findByAccount_Id(id);
            if (customerCart == null) {
                throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Impossible to delete the cart. No carts are associate to this customer.");
            } else {
                cartRepository.delete(customerCart);
            }
        }
    }

    /**
     * Associate the cart item to a cart
     *
     * @param id          user's id
     * @param cartItemDto
     * @author Sonia Ly
     */
//    public void addItemToCart(int id, CartItemDto cartItemDto, CartDto cartDto) {
//        Account customer = accountRepository.findById(id).orElse(null);
//
//        if (customer == null) {
//            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + id + " found.");
//        } else {
//            // See if the user already has a cart
//            Cart customerCart = cartRepository.findByAccount_Id(id);
//            if (customerCart == null) {
//                throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Cannot add an item to the cart. This customer doesn't have a cart!");
//            }
//
//            cartItemDto.setCart(cartDto);
//        }
//    }

    /**
     * Given a user's id, find their cart's total
     * @param id
     * @author Sonia Ly
     */
    public int findCartTotalPrice(int id) {
        int total = 0;

        // Check if the user already as a cart (a user should only have one cart at the time)
        Account customer = accountRepository.findById(id).orElse(null);

        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + id + " found.");
        } else {
            // See if the user already has a cart
            Cart customerCart = cartRepository.findByAccount_Id(id);
            if (customerCart == null) {
                throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "This user's cart is empty. Cannot calculate the cart's total.");
            }

            List<CartItem> cartItems = cartItemRepository.findByCartItemId_Cart(customerCart);

            for (CartItem cartItem: cartItems) {
                ItemType itemType = cartItem.getCartItemId().getItemType();
                total += itemType.getId();
            }
        }
        return total;
    }


}
