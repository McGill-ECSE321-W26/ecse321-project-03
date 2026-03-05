package ca.mcgill.ecse321.ecse321project03.dto;


import ca.mcgill.ecse321.ecse321project03.model.OrderItem;


public class OrderItemDto {
        
    
    // displayed fields
    private int quantity;       
    private ItemTypeDto itemType;
    private int orderId;

    public OrderItemDto(OrderItem orderItem, int orderId) {
        this.quantity = orderItem.getQuantity();
        this.itemType = new ItemTypeDto(orderItem.getOrderItemId().getItemType());
        this.orderId = orderId;
    }

    @SuppressWarnings("unused")
    public OrderItemDto() {
    }

    // Getter methods
    public int getQuantity() {
        return quantity;
    }
    public ItemTypeDto getItemType() {
        return itemType;
    }
    public int getOrderId() {
        return orderId;
    }
}
