package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/


import java.sql.Date;

// line 44 "model.ump"
// line 119 "model.ump"
public class Order
{

    //------------------------
    // ENUMERATIONS
    //------------------------

    public enum OrderState { Ordered, InPreparation, Prepared, Delivered, Cancelled }

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Order Attributes
    private int id;
    private Date orderDate;
    private Date delivDate;
    private OrderState orderState;

    //Order Associations
    private Account customer;
    private Employee assignee;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Order(int aId, Date aOrderDate, Date aDelivDate, OrderState aOrderState, Account aCustomer, Employee aAssignee)
    {
        id = aId;
        orderDate = aOrderDate;
        delivDate = aDelivDate;
        orderState = aOrderState;
        if (!setCustomer(aCustomer))
        {
            throw new RuntimeException("Unable to create Order due to aCustomer. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setAssignee(aAssignee))
        {
            throw new RuntimeException("Unable to create Order due to aAssignee. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setId(int aId)
    {
        boolean wasSet = false;
        id = aId;
        wasSet = true;
        return wasSet;
    }

    public boolean setOrderDate(Date aOrderDate)
    {
        boolean wasSet = false;
        orderDate = aOrderDate;
        wasSet = true;
        return wasSet;
    }

    public boolean setDelivDate(Date aDelivDate)
    {
        boolean wasSet = false;
        delivDate = aDelivDate;
        wasSet = true;
        return wasSet;
    }

    public boolean setOrderState(OrderState aOrderState)
    {
        boolean wasSet = false;
        orderState = aOrderState;
        wasSet = true;
        return wasSet;
    }

    public int getId()
    {
        return id;
    }

    public Date getOrderDate()
    {
        return orderDate;
    }

    public Date getDelivDate()
    {
        return delivDate;
    }

    public OrderState getOrderState()
    {
        return orderState;
    }
    /* Code from template association_GetOne */
    public Account getCustomer()
    {
        return customer;
    }
    /* Code from template association_GetOne */
    public Employee getAssignee()
    {
        return assignee;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setCustomer(Account aNewCustomer)
    {
        boolean wasSet = false;
        if (aNewCustomer != null)
        {
            customer = aNewCustomer;
            wasSet = true;
        }
        return wasSet;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setAssignee(Employee aNewAssignee)
    {
        boolean wasSet = false;
        if (aNewAssignee != null)
        {
            assignee = aNewAssignee;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        customer = null;
        assignee = null;
    }


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "orderDate" + "=" + (getOrderDate() != null ? !getOrderDate().equals(this)  ? getOrderDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "delivDate" + "=" + (getDelivDate() != null ? !getDelivDate().equals(this)  ? getDelivDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "orderState" + "=" + (getOrderState() != null ? !getOrderState().equals(this)  ? getOrderState().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "customer = "+(getCustomer()!=null?Integer.toHexString(System.identityHashCode(getCustomer())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "assignee = "+(getAssignee()!=null?Integer.toHexString(System.identityHashCode(getAssignee())):"null");
    }
}

