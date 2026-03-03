package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

@Entity
public class CartItem {

    //------------------------
    // Composite primary class for CartItem
    //------------------------
    @Embeddable
    public static class CartItemId implements Serializable {

        // Associations
        @ManyToOne
        private ItemType itemType;
        @ManyToOne
        private Cart cart;

        //------------------------
        // CONSTRUCTOR
        //------------------------
        public CartItemId() {
            super();
        }

        public CartItemId(ItemType itemType, Cart cart) {
            super();
            this.itemType = itemType;
            this.cart = cart;
        }

        //------------------------
        // Getters
        //------------------------
        public ItemType getItemType() {
            return itemType;
        }

        public Cart getCart() {
            return cart;
        }


        // Comparison method
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CartItemId that = (CartItemId) o;

            Integer thisItemTypeId = (itemType == null) ? null : itemType.getId();
            Integer thatItemTypeId = (that.itemType == null) ? null : that.itemType.getId();
            if (thisItemTypeId == null ? thatItemTypeId != null : !thisItemTypeId.equals(thatItemTypeId)) return false;

            Integer thisCartId = (cart == null) ? null : cart.getId();
            Integer thatCartId = (that.cart == null) ? null : that.cart.getId();
            return thisCartId == null ? thatCartId == null : thisCartId.equals(thatCartId);
        }

        // hashcode method
        @Override
        public int hashCode() {
            Integer itemTypeId = (itemType == null) ? null : itemType.getId();
            Integer cartId = (cart == null) ? null : cart.getId();
            int result = 17;
            result = 31 * result + (itemTypeId == null ? 0 : itemTypeId.hashCode());
            result = 31 * result + (cartId == null ? 0 : cartId.hashCode());
            return result;
        }
    }

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Registration attributes
    private int quantity;
    @EmbeddedId
    private CartItemId cartItemId;
    private int cachedHashCode;


    //------------------------
    // CONSTRUCTOR
    //------------------------
    public CartItem() {

    }

    public CartItem(CartItemId cartItemId, int quantity) {
        cachedHashCode = -1;
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }


    //------------------------
    // INTERFACE
    //------------------------

    // Setter method
    public boolean setCartItemId(CartItemId aNewCartItemId) {
        boolean wasSet = false;
        if (aNewCartItemId != null) {
            cartItemId = aNewCartItemId;
            wasSet = true;
        }

        return wasSet;
    }

    public void setQuantity(int quantity) {this.quantity = quantity;}

    // Getter method
    public CartItemId getCartItemId() {
        return cartItemId;
    }

    public int getQuantity() {return quantity;}



    // Comparison method
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        CartItem compareTo = (CartItem) obj;

        if (getCartItemId() == null && compareTo.getCartItemId() != null) {
            return false;
        } else if (getCartItemId() != null && !getCartItemId().equals(compareTo.getCartItemId())) {
            return false;
        }

        return true;
    }

    // hashcode method
    public int hashCode() {
        if (cachedHashCode != -1) {
            return cachedHashCode;
        }
        cachedHashCode = 17;
        if (getCartItemId() != null) {
            cachedHashCode = cachedHashCode * 23 + getCartItemId().hashCode();
        } else {
            cachedHashCode = cachedHashCode * 23;
        }

        return cachedHashCode;
    }

    // Delete method
    public void delete() {
        cartItemId = null;
    }

}