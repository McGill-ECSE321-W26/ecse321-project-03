package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/


// line 84 "model.ump"
// line 154 "model.ump"

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

@Entity
public class CartItem {
    @Embeddable
    public static class CartItemId implements Serializable {

        private int quantity;
        @ManyToOne
        private ItemType itemType;
        @ManyToOne
        private Cart cart;

        public CartItemId() {
            super();
        }

        public CartItemId(ItemType itemType, Cart cart, int quantity) {
            super();
            this.itemType = itemType;
            this.cart = cart;
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        /* Code from template association_GetOne */
        public ItemType getItemType() {
            return itemType;
        }

        /* Code from template association_GetOne */
        public Cart getCart() {
            return cart;
        }
    }
        //------------------------
        // MEMBER VARIABLES
        //------------------------

        //Registration Associations
        @EmbeddedId
        private CartItemId cartItemId;
        private int cachedHashCode;


        //------------------------
        // CONSTRUCTOR
        //------------------------
        public CartItem(){

        }

        public CartItem(CartItemId cartItemId) {
            cachedHashCode = -1;
            this.cartItemId = cartItemId;
        }


        //------------------------
        // INTERFACE
        //------------------------

        public CartItemId getCartItemId(){
            return cartItemId;
        }
        public boolean setCartItemId(CartItemId aNewCartItemId){
            boolean wasSet = false;
            if (aNewCartItemId != null)
            {
                cartItemId = aNewCartItemId;
                wasSet = true;
            }

            return wasSet;
        }

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

        public void delete() {
            cartItemId = null;
        }

    }



