package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.Order;


import java.sql.Date;

public class OrderResponseDto { // This class is for the attributes that must be displayed to the UI

    private int id;
    private Date orderDate;
    private Date delivDate;
    private String orderState;
    private int customerId;
    private int assigneeId;

    @SuppressWarnings("unused")
    public OrderResponseDto() {
    }

    //------------------------
    // CONSTRUCTOR
    //------------------------
    public OrderResponseDto(Order order) {
        this.id = order.getId();
        this.orderDate = order.getOrderDate();
        this.delivDate = order.getDelivDate();
        this.orderState = order.getOrderState().toString();
        this.customerId = order.getCustomer().getId();
        this.assigneeId = order.getAssignee().getId();
    }


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
}
