package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

@Entity
public class OrderItem {


    //------------------------
    // Composite primary class for CartItem
    //------------------------
    @Embeddable
    public static class OrderItemId implements Serializable {


        // Associations
        @ManyToOne
        private ItemType itemType;
        @ManyToOne
        private Order order;

        //------------------------
        // CONSTRUCTOR
        //------------------------
        public OrderItemId() {
            super();
        }

        public OrderItemId(ItemType itemType, Order order) {
            super();
            this.itemType = itemType;
            this.order = order;
        }

        //------------------------
        // GETTER
        //------------------------

        /* Code from template association_GetOne */
        public ItemType getItemType() {
            return itemType;
        }

        /* Code from template association_GetOne */
        public Order getOrder() {
            return order;
        }

        // Comparison method
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OrderItemId that = (OrderItemId) o;

            // Compare by referenced IDs if possible (safer than comparing whole objects)
            Integer thisItemTypeId = (itemType == null) ? null : itemType.getId();
            Integer thatItemTypeId = (that.itemType == null) ? null : that.itemType.getId();
            if (thisItemTypeId == null ? thatItemTypeId != null : !thisItemTypeId.equals(thatItemTypeId)) return false;

            Integer thisOrderId = (order == null) ? null : order.getId();
            Integer thatOrderId = (that.order == null) ? null : that.order.getId();
            return thisOrderId == null ? thatOrderId == null : thisOrderId.equals(thatOrderId);
        }

        // hashcode method
        @Override
        public int hashCode() {
            Integer itemTypeId = (itemType == null) ? null : itemType.getId();
            Integer orderId = (order == null) ? null : order.getId();
            int result = 17;
            result = 31 * result + (itemTypeId == null ? 0 : itemTypeId.hashCode());
            result = 31 * result + (orderId == null ? 0 : orderId.hashCode());
            return result;
        }
    }

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    // Attributes
    private int quantity;
    @EmbeddedId
    private OrderItemId orderItemId;
    private int cachedHashCode;

    //------------------------
    // CONSTRUCTOR
    //------------------------
    public OrderItem() {
    }

    public OrderItem(OrderItemId orderItemId, int quantity) {
        cachedHashCode = -1;
        this.orderItemId = orderItemId;
        this.quantity = quantity;
    }

    //------------------------
    // INTERFACE
    //------------------------

    // Setter
    public boolean setOrderItemId(OrderItemId aNewOrderItemId) {
        boolean wasSet = false;
        if (aNewOrderItemId != null) {
            orderItemId = aNewOrderItemId;
            wasSet = true;
        }

        return wasSet;
    }

    public void setQuantity(int quantity) {this.quantity = quantity;}

    // Getter method
    public OrderItemId getOrderItemId() {
        return orderItemId;
    }

    public int getQuantity() {return quantity;}

    // Comparison method
    @Override
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

    // HashCode method
    @Override
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

    // Delete method
    public void delete() {
        orderItemId = null;
    }


}
