package ca.mcgill.ecse321.ecse321project03.dto;


import ca.mcgill.ecse321.ecse321project03.model.OrderItem;


public class OrderItemDto {
        
    
    // displayed fields
    private int quantity;       
    private ItemTypeDto itemType;
    private OrderDto order;

    public OrderItemDto(OrderItem orderItem) {
        this.quantity = orderItem.getOrderItemId().getQuantity();
        this.itemType = new ItemTypeDto(orderItem.getOrderItemId().getItemType());
        this.order = new OrderDto(orderItem.getOrderItemId().getOrder());
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
    public OrderDto getOrder() {
        return order;
    }
}
