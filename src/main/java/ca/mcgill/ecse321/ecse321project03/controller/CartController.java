package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.CartDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemResponseDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemQuantityUpdateDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemImageDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemTypeDto;
import ca.mcgill.ecse321.ecse321project03.model.Cart;
import ca.mcgill.ecse321.ecse321project03.model.CartItem;
import ca.mcgill.ecse321.ecse321project03.model.ItemImage;
import ca.mcgill.ecse321.ecse321project03.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;


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
     * @return the created cart
     */
    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public CartDto createNewCart(@PathVariable int id) {
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
     * Retrieves all items in a user's cart.
     *
     * @param id the id of the user
     * @return the list of cart items in the user's cart
     */
    @GetMapping("/{id}/items")
    public List<CartItemResponseDto> getCartItems(@PathVariable int id) {
        return cartService.getCartItems(id).stream()
                .map(cartItem -> {
                    int generalItemId = cartItem.getCartItemId().getItemType().getGeneralItem().getId();

                    ItemImage firstImage = cartService.getFirstImageForCartOrOrderItem(generalItemId);
                    ItemImageDto imageDto = firstImage == null ? null : new ItemImageDto(firstImage);

                    return new CartItemResponseDto(
                            cartItem.getQuantity(),
                            new ItemTypeDto(cartItem.getCartItemId().getItemType()),
                            new CartDto(cartItem.getCartItemId().getCart()),
                            imageDto
                    );
                })
                .toList();
    }

    /**
     * Removes one item type entirely from a user's cart.
     *
     * @param id the id of the user
     * @param itemTypeId the id of the item type to remove
     */
    @DeleteMapping("/{id}/items/{itemTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItemFromCart(@PathVariable int id, @PathVariable int itemTypeId) {
        cartService.removeItemFromCart(id, itemTypeId);
    }

    /**
     * Updates the quantity of one item type in a user's cart.
     *
     * @param id the id of the user
     * @param itemTypeId the id of the item type to update
     * @param updateDto the cart item data containing the new quantity
     * @return the updated cart item
     */
    @PutMapping("/{id}/items/{itemTypeId}")
    public CartItemDto updateCartItemQuantity(@PathVariable int id, @PathVariable int itemTypeId,
                                              @RequestBody CartItemQuantityUpdateDto updateDto) {
        CartItem cartItem = cartService.updateCartItemQuantity(id, itemTypeId, updateDto.getQuantity());
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
