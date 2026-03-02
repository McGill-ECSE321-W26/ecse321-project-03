package ca.mcgill.ecse321.ecse321project03.dto;


import jakarta.validation.constraints.NotNull;

import java.sql.Date;

//Reference: Tutoral 7
public class OrderCreationDto {

    // displayed fields


    // no orderDate attribute because it is set when the order object is created (current time)

    // no orderStatus attribute because it is set when the object is created (to Ordered)
    @NotNull(message = "Customer id linked to the order must not be null")
    private int customerId; // I guess the customer must enter their id when making an order, otherwise no way to know who made the order ?



    @NotNull(message = "Delivery date must not be null")
    private Date delivDate; // The delivery date is chosen by the customer

    @NotNull(message = "Employee id assigned to the order must not be null")
    private int assigneeId;


    //------------------------
    // CONSTRUCTOR
    //------------------------
    public OrderCreationDto(int customerId, Date delivDate, int assigneeId) {
        this.customerId = customerId;
        this.delivDate = delivDate;
        this.assigneeId = assigneeId;
    }

    @SuppressWarnings("unused")
    public OrderCreationDto() { // Needed by Jackson to deserialize JSON

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
