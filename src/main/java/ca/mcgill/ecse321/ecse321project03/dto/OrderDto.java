package ca.mcgill.ecse321.ecse321project03.dto;


import jakarta.validation.constraints.NotNull;

import java.sql.Date;

//Reference: Tutoral 7
public class OrderDto {
    
    // displayed fields


    // no orderDate attribute because it is set when the order object is created (current time)

    // no orderStatus attribute because it is set when the object is created (to Ordered)
    @NotNull(message = "Customer id linked to the order must not be null")
    private int customerId;

    @NotNull(message = "Delivery date must not be null")
    private Date delivDate;

    @NotNull(message = "Employee id assigned to the order must not be null")
    private int assigneeId;


    //------------------------
    // CONSTRUCTOR
    //------------------------
    public OrderDto(int customerId,  Date delivDate, int assigneeId) {
        this.customerId = customerId;
        this.delivDate = delivDate;
        this.assigneeId = assigneeId;
    }

    @SuppressWarnings("unused")
    public OrderDto() { // Needed by Jackson to deserialize JSON

    }

    //------------------------
    // INTERFACE
    //------------------------


    // Getter methods

    public int getCustomerId() {
        return customerId;
    }

    public Date getDelivDate() {
        return delivDate;
    }

    public int getAssigneeId() {
        return assigneeId;
    }

    
}
