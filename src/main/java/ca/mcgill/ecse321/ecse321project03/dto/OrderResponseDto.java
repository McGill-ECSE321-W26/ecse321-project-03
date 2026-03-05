package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.OrderItem;


import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class OrderResponseDto { // This class is for the attributes that must be displayed to the UI


    //------------------------
    // Attributes important to display in the UI
    //------------------------

    private int id;
    private Date orderDate;
    private Date delivDate;
    private String orderState;
    private int customerId;
    private int assigneeId;
    private List<OrderItemDto> items;

    @SuppressWarnings("unused")
    public OrderResponseDto() {
    }

    //------------------------
    // CONSTRUCTOR
    //------------------------

    /**
     * Constructor that retrieves important data from an order object in a transfer object
     * @author Wissam Abchiche
     * @param order order to retrieve its important data
     */
    public OrderResponseDto(Order order, List<OrderItem> orderItems) {
        this.id = order.getId();
        this.orderDate = order.getOrderDate();
        this.delivDate = order.getDelivDate();
        this.orderState = order.getOrderState().toString();
        this.customerId = order.getCustomer().getId();
        this.assigneeId = (order.getAssignee() == null) ? 0 : order.getAssignee().getId();

        this.items = new ArrayList<>();
        for (OrderItem oi : orderItems) {
            items.add(new OrderItemDto(oi, this.id));
        }
    }


    //------------------------
    // Getters
    //------------------------
    public int getId() {
        return id;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public Date getDelivDate() {
        return delivDate;
    }

    public String getOrderState() {
        return orderState;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getAssigneeId() {
        return assigneeId;
    }

    public List<OrderItemDto> getItems() {return items;}
}
