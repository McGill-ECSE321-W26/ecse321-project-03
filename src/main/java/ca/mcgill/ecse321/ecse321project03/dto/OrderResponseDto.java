package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.OrderItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderResponseDto { // This class is for the attributes that must be displayed to the UI


    //------------------------
    // Attributes important to display in the UI
    //------------------------

    private int id;
    private Date orderDate;
    private Date delivDate;
    private String orderState;
    private int customerId;
    private String customerName;
    private int assigneeId;
    private String assigneeName;
    private List<OrderItemDto> items;
    private double totalPriceAfterDiscount;
    private String address;

    @SuppressWarnings("unused")
    public OrderResponseDto() {
    }

    //------------------------
    // CONSTRUCTOR
    //------------------------

    /**
     * Constructor that retrieves important data from an order object in a transfer object
     *
     * @param order order to retrieve its important data
     * @author Wissam Abchiche
     */
    public OrderResponseDto(Order order, List<OrderItemDto> orderItemsDto) {
        this.id = order.getId();
        this.orderDate = order.getOrderDate();
        this.delivDate = order.getDelivDate();
        this.orderState = (order.getOrderState() == null) ? "Unknown" : order.getOrderState().toString();
        this.customerId = (order.getCustomer() == null) ? 0 : order.getCustomer().getId();
        this.customerName = (order.getCustomer() == null)
                ? "Unknown customer"
                : order.getCustomer().getUsername();

        this.assigneeId = (order.getAssignee() == null) ? 0 : order.getAssignee().getId();
        this.totalPriceAfterDiscount = order.getTotalPriceAfterDiscount();
        this.address = order.getAddress();
        this.assigneeName = (order.getAssignee() == null || order.getAssignee().getAccount() == null)
                ? "Not assigned"
                : order.getAssignee().getAccount().getUsername();

        this.items = new ArrayList<>(orderItemsDto);
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
    public String getAddress(){
        return address;
    }

    public double getTotalPrice() {
        return totalPriceAfterDiscount;
    }
    
    public String getCustomerName() {
        return customerName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }
}
