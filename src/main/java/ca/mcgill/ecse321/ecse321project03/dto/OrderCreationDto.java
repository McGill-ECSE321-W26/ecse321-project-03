package ca.mcgill.ecse321.ecse321project03.dto;


import jakarta.validation.constraints.NotNull;

import java.util.Date;

//Reference: Tutoral 7
public class OrderCreationDto {


    // no orderDate attribute because it is set when the order object is created (current time)

    // no orderStatus attribute because it is set when the object is created (to Ordered)

    // no employee attribute as the employee linked to the order gets assigned later (either an employee assigns himself or a manager assigns an employee)

    //------------------------
    // Attributes important to retrieve from the UI
    //------------------------
    @NotNull(message = "Customer id linked to the order must not be null")
    private int customerId; // The customer must enter their id when making an order, otherwise no way to know who made the order ?

    @NotNull(message = "Delivery date must not be null")
    private Date delivDate; // The delivery date is chosen by the customer


    //------------------------
    // CONSTRUCTOR
    //------------------------


    /**
     * Constructor that creates an order transfer object
     * @author Wissam Abchiche
     * @param customerId the customer who made the order
     * @param delivDate the chosen delivery date by the customer
     */
    public OrderCreationDto(int customerId, Date delivDate) {
        this.customerId = customerId;
        this.delivDate = delivDate;
    }

    @SuppressWarnings("unused")
    public OrderCreationDto() { // Needed by Jackson to deserialize JSON

    }

    //------------------------
    // Getters
    //------------------------

    public int getCustomerId() {
        return customerId;
    }

    public Date getDelivDate() {
        return delivDate;
    }


    
}
