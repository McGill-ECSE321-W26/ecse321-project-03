package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/


import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

// line 53 "model.ump"
// line 134 "model.ump"
@Entity
public class OrderItem {

    @Embeddable
    public static class OrderItemId implements Serializable {

        private int quantity;

        @ManyToOne
        private ItemType itemType;
        @ManyToOne
        private Order order;

        public OrderItemId() {
            super();
        }

        public OrderItemId(ItemType itemType, Order order, int quantity) {
            super();
            this.itemType = itemType;
            this.order = order;
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
        public Order getOrder() {
            return order;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OrderItemId that = (OrderItemId) o;

            if (quantity != that.quantity) return false;

            // Compare by referenced IDs if possible (safer than comparing whole objects)
            Integer thisItemTypeId = (itemType == null) ? null : itemType.getId();
            Integer thatItemTypeId = (that.itemType == null) ? null : that.itemType.getId();
            if (thisItemTypeId == null ? thatItemTypeId != null : !thisItemTypeId.equals(thatItemTypeId)) return false;

            Integer thisOrderId = (order == null) ? null : order.getId();
            Integer thatOrderId = (that.order == null) ? null : that.order.getId();
            return thisOrderId == null ? thatOrderId == null : thisOrderId.equals(thatOrderId);
        }

        @Override
        public int hashCode() {
            int result = quantity;
            Integer itemTypeId = (itemType == null) ? null : itemType.getId();
            Integer orderId = (order == null) ? null : order.getId();
            result = 31 * result + (itemTypeId == null ? 0 : itemTypeId.hashCode());
            result = 31 * result + (orderId == null ? 0 : orderId.hashCode());
            return result;
        }
    }

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Registration Associations
    @EmbeddedId
    private OrderItemId orderItemId;
    private int cachedHashCode;

    //------------------------
    // CONSTRUCTOR
    //------------------------
    public OrderItem(){}

    public OrderItem(OrderItemId orderItemId) {
        cachedHashCode = -1;
        this.orderItemId = orderItemId;
    }

    //------------------------
    // INTERFACE
    //------------------------

    public OrderItemId getOrderItemId(){
        return orderItemId;
    }

    public boolean setOrderItemId(OrderItemId aNewOrderItemId){
        boolean wasSet = false;
        if (aNewOrderItemId != null)
        {
            orderItemId = aNewOrderItemId;
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

        OrderItem compareTo = (OrderItem) obj;

        if (getOrderItemId() == null && compareTo.getOrderItemId() != null) {
            return false;
        } else if (getOrderItemId() != null && !getOrderItemId().equals(compareTo.getOrderItemId())) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        if (cachedHashCode != -1) {
            return cachedHashCode;
        }
        cachedHashCode = 17;
        if (getOrderItemId() != null) {
            cachedHashCode = cachedHashCode * 23 + getOrderItemId().hashCode();
        } else {
            cachedHashCode = cachedHashCode * 23;
        }

        return cachedHashCode;
    }

    public void delete() {
        orderItemId = null;
    }


}
