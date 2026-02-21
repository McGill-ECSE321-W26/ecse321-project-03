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

        // Attributes
        private int quantity;

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

        public CartItemId(ItemType itemType, Cart cart, int quantity) {
            super();
            this.itemType = itemType;
            this.cart = cart;
            this.quantity = quantity;
        }

        //------------------------
        // Getters
        //------------------------
        public int getQuantity() {
            return quantity;
        }

        public ItemType getItemType() {
            return itemType;
        }

        public Cart getCart() {
            return cart;
        }
    }

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Registration attributes
    @EmbeddedId
    private CartItemId cartItemId;
    private int cachedHashCode;


    //------------------------
    // CONSTRUCTOR
    //------------------------
    public CartItem() {

    }

    public CartItem(CartItemId cartItemId) {
        cachedHashCode = -1;
        this.cartItemId = cartItemId;
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

    // Getter method
    public CartItemId getCartItemId() {
        return cartItemId;
    }


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



