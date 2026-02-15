package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 54 "model.ump"
// line 124 "model.ump"
public class OrderItem
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //OrderItem Attributes
    private int quantity;

    //OrderItem Associations
    private Order order;
    private ItemType itemType;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public OrderItem(int aQuantity, Order aOrder, ItemType aItemType)
    {
        quantity = aQuantity;
        if (!setOrder(aOrder))
        {
            throw new RuntimeException("Unable to create OrderItem due to aOrder. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setItemType(aItemType))
        {
            throw new RuntimeException("Unable to create OrderItem due to aItemType. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setQuantity(int aQuantity)
    {
        boolean wasSet = false;
        quantity = aQuantity;
        wasSet = true;
        return wasSet;
    }

    public int getQuantity()
    {
        return quantity;
    }
    /* Code from template association_GetOne */
    public Order getOrder()
    {
        return order;
    }
    /* Code from template association_GetOne */
    public ItemType getItemType()
    {
        return itemType;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setOrder(Order aNewOrder)
    {
        boolean wasSet = false;
        if (aNewOrder != null)
        {
            order = aNewOrder;
            wasSet = true;
        }
        return wasSet;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setItemType(ItemType aNewItemType)
    {
        boolean wasSet = false;
        if (aNewItemType != null)
        {
            itemType = aNewItemType;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        order = null;
        itemType = null;
    }


    public String toString()
    {
        return super.toString() + "["+
                "quantity" + ":" + getQuantity()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "order = "+(getOrder()!=null?Integer.toHexString(System.identityHashCode(getOrder())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "itemType = "+(getItemType()!=null?Integer.toHexString(System.identityHashCode(getItemType())):"null");
    }
}