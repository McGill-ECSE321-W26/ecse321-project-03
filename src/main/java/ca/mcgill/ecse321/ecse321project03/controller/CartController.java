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
import org.springframework.web.bind.annotation.*;
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
     * @return the created cart
     */
    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public CartDto createNewCart(@PathVariable int id, @RequestBody CartCreationDto cartCreationDto) {
        Cart cart = cartService.createNewCart(id);
        return new CartDto(cart);
    }

    /**
     * Deletes a user's cart.
     *
     * @param id the id of the user
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCart(@PathVariable int id) {
        cartService.deleteCart(id);
    }

    /**
     * Adds an item to a user's cart.
     *
     * @param id the id of the user
     * @param cartItemDto the cart item data containing the item type and quantity
     * @return the created cart item
     */
    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemDto addItemToCart(@PathVariable int id, @RequestBody CartItemDto cartItemDto) {
        CartItem cartItem = cartService.addItemToCart(id, cartItemDto);
        return new CartItemDto(cartItem);
    }

    /**
     * Retrieves the total price of all items in a user's cart.
     *
     * @param id the id of the user
     * @return the total cart price
     */
    @GetMapping("/{id}/total")
    public double findCartTotalPrice(@PathVariable int id) {
        return cartService.findCartTotalPrice(id);
    }
}
