package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;

import java.util.Date;


@Entity
@Table(name = "orders")
public class Order {

    //------------------------
    // ENUMERATIONS
    //------------------------

    public enum OrderState {Ordered, InPreparation, Prepared, Delivered, Cancelled}

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Order Attributes
    @Id
    @GeneratedValue
    private int id;
    private Date orderDate;
    private Date delivDate;

    @Enumerated(EnumType.ORDINAL)
    private OrderState orderState;

    //Order Associations
    @ManyToOne
    private Account customer;
    @ManyToOne()
    @JoinColumn(nullable = true)
    private Employee assignee;

    private double totalPriceAfterDiscount;

    //------------------------
    // CONSTRUCTOR
    //------------------------
    protected Order() {
    }

    public Order(Date aOrderDate, Date aDelivDate, OrderState aOrderState, Account aCustomer, Employee aAssignee) {
        orderDate = aOrderDate;
        delivDate = aDelivDate;
        orderState = aOrderState;
        assignee = aAssignee;
        totalPriceAfterDiscount = 0;
        if (!setCustomer(aCustomer)) {
            throw new RuntimeException("Unable to create Order due to aCustomer. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    //------------------------
    // INTERFACE
    //------------------------

    // Setter methods
    public boolean setId(int aId) {
        boolean wasSet = false;
        id = aId;
        wasSet = true;
        return wasSet;
    }

    public boolean setOrderDate(Date aOrderDate) {
        boolean wasSet = false;
        orderDate = aOrderDate;
        wasSet = true;
        return wasSet;
    }

    public boolean setDelivDate(Date aDelivDate) {
        boolean wasSet = false;
        delivDate = aDelivDate;
        wasSet = true;
        return wasSet;
    }

    public boolean setOrderState(OrderState aOrderState) {
        boolean wasSet = false;
        orderState = aOrderState;
        wasSet = true;
        return wasSet;
    }

    public boolean setCustomer(Account aNewCustomer) {
        boolean wasSet = false;
        if (aNewCustomer != null) {
            customer = aNewCustomer;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean setAssignee(Employee aNewAssignee) {
        boolean wasSet = false;
        assignee = aNewAssignee;
        wasSet = true;
        return wasSet;
    }

    public boolean setTotalPriceAfterDiscount(double aFinalPriceAfterDiscount) {
        boolean wasSet = false;
        totalPriceAfterDiscount = aFinalPriceAfterDiscount;
        wasSet = true;
        return wasSet;
    }

    // Getter methods
    public int getId() {
        return id;
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

    /* Code from template association_GetOne */
    public Account getCustomer() {
        return customer;
    }

    /* Code from template association_GetOne */
    public Employee getAssignee() {
        return assignee;
    }

    public double getTotalPriceAfterDiscount() {
        return totalPriceAfterDiscount;
    }


    // Delete method
    public void delete() {
        customer = null;
        assignee = null;
    }

    // toString method
    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "orderDate" + "=" + (getOrderDate() != null ? !getOrderDate().equals(this) ? getOrderDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "delivDate" + "=" + (getDelivDate() != null ? !getDelivDate().equals(this) ? getDelivDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "orderState" + "=" + (getOrderState() != null ? !getOrderState().equals(this) ? getOrderState().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "customer = " + (getCustomer() != null ? Integer.toHexString(System.identityHashCode(getCustomer())) : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "assignee = " + (getAssignee() != null ? Integer.toHexString(System.identityHashCode(getAssignee())) : "null");
    }
}

