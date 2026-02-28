package ca.mcgill.ecse321.ecse321project03.dto;


import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.Order.OrderState;
import java.sql.Date;

//Reference: Tutoral 7
public class OrderDto {
    
    // displayed fields
    private int id;
    private AccountResponseDto customer;
    private Date orderDate;
    private Date delivDate;
    private OrderState orderState;
    private EmployeeDto assignee;


    //------------------------
    // CONSTRUCTOR
    //------------------------
    public OrderDto(Order order) {
        this.id = order.getId();
        this.customer = new AccountResponseDto(order.getCustomer());
        this.orderDate = order.getOrderDate();
        this.delivDate = order.getDelivDate();
        this.orderState = order.getOrderState();
        this.assignee = new EmployeeDto(order.getAssignee());
    }

    @SuppressWarnings("unused")
    public OrderDto() {

    }

    //------------------------
    // INTERFACE
    //------------------------


    // Getter methods
    public int getId() {
        return id;
    }

    public AccountResponseDto getCustomer() {
        return customer;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public Date getDelivDate() {
        return delivDate;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public EmployeeDto getAssignee() {
        return assignee;
    }

    
}
