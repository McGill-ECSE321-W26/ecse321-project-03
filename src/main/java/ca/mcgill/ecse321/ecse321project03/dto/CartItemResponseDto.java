package ca.mcgill.ecse321.ecse321project03.dto;

public class CartItemResponseDto {

    private int quantity;
    private ItemTypeDto itemType;
    private CartDto cart;
    private ItemImageDto image;

    public CartItemResponseDto() {
    }

    public CartItemResponseDto(int quantity, ItemTypeDto itemType, CartDto cart, ItemImageDto image) {
        this.quantity = quantity;
        this.itemType = itemType;
        this.cart = cart;
        this.image = image;
    }

    public int getQuantity() {
        return quantity;
    }

    public ItemTypeDto getItemType() {
        return itemType;
    }

    public CartDto getCart() {
        return cart;
    }

    public ItemImageDto getImage() {
        return image;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setItemType(ItemTypeDto itemType) {
        this.itemType = itemType;
    }

    public void setCart(CartDto cart) {
        this.cart = cart;
    }

    public void setImage(ItemImageDto image) {
        this.image = image;
    }
}