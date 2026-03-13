package ca.mcgill.ecse321.ecse321project03.dto;
import ca.mcgill.ecse321.ecse321project03.model.CartItem;


//Reference: Tutorial 7
public class CartItemDto {
    
    // diplayed fields
    private int quantity;
    private ItemTypeDto itemType;
    private CartDto cart;

    public CartItemDto(CartItem cartItem) {
        this.quantity = cartItem.getQuantity();
        this.itemType = new ItemTypeDto(cartItem.getCartItemId().getItemType());
        this.cart = new CartDto(cartItem.getCartItemId().getCart());
    }

    // Getter methods
    public int getQuantity() {
        return quantity;
    }
    public ItemTypeDto getItemType() {
        return itemType;
    }
    public CartDto getCart() {
        return cart;
    }

}
