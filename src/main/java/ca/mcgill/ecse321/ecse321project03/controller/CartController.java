package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.CartCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Cart;
import ca.mcgill.ecse321.ecse321project03.model.CartItem;
import ca.mcgill.ecse321.ecse321project03.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for handling cart-related API endpoints.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Creates a new cart for a user.
     *
     * @param id the id of the user
     * @param cartCreationDto the cart creation data
     * @return a ResponseEntity containing the created CartDto if successful,
     *         or an ErrorDto if cart creation fails
     */
    @PostMapping("/{id}")
    public ResponseEntity<?> createNewCart(@PathVariable int id, @RequestBody CartCreationDto cartCreationDto) {
        try {
            Cart cart = cartService.createNewCart(id);
            return new ResponseEntity<>(new CartDto(cart), HttpStatus.CREATED);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Deletes a user's cart.
     *
     * @param id the id of the user
     * @return a ResponseEntity with NO_CONTENT status if successful,
     *         or an ErrorDto if deletion fails
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCart(@PathVariable int id) {
        try {
            cartService.deleteCart(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Adds an item to a user's cart.
     *
     * @param id the id of the user
     * @param cartItemDto the cart item data containing the item type and quantity
     * @return a ResponseEntity containing the saved CartItemDto if successful,
     *         or an ErrorDto if the operation fails
     */
    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItemToCart(@PathVariable int id, @RequestBody CartItemDto cartItemDto) {
        try {
            CartItem cartItem = cartService.addItemToCart(id, cartItemDto);
            return new ResponseEntity<>(new CartItemDto(cartItem), HttpStatus.CREATED);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Retrieves the total price of all items in a user's cart.
     *
     * @param id the id of the user
     * @return a ResponseEntity containing the cart total price if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/{id}/total")
    public ResponseEntity<?> findCartTotalPrice(@PathVariable int id) {
        try {
            double total = cartService.findCartTotalPrice(id);
            return new ResponseEntity<>(total, HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }
}
