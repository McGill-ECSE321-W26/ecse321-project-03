package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 53 "model.ump"
// line 131 "model.ump"
public class OrderItem
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //OrderItem Associations
    private ItemType itemType;
    private Order order;

    //Helper Variables
    private int cachedHashCode;
    private boolean canSetItemType;
    private boolean canSetOrder;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public OrderItem(ItemType aItemType, Order aOrder)
    {
        cachedHashCode = -1;
        canSetItemType = true;
        canSetOrder = true;
        if (!setItemType(aItemType))
        {
            throw new RuntimeException("Unable to create OrderItem due to aItemType. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setOrder(aOrder))
        {
            throw new RuntimeException("Unable to create OrderItem due to aOrder. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    //------------------------
    // INTERFACE
    //------------------------
    /* Code from template association_GetOne */
    public ItemType getItemType()
    {
        return itemType;
    }
    /* Code from template association_GetOne */
    public Order getOrder()
    {
        return order;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setItemType(ItemType aNewItemType)
    {
        boolean wasSet = false;
        if (!canSetItemType) { return false; }
        if (aNewItemType != null)
        {
            itemType = aNewItemType;
            wasSet = true;
        }
        return wasSet;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setOrder(Order aNewOrder)
    {
        boolean wasSet = false;
        if (!canSetOrder) { return false; }
        if (aNewOrder != null)
        {
            order = aNewOrder;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean equals(Object obj)
    {
        if (obj == null) { return false; }
        if (!getClass().equals(obj.getClass())) { return false; }

        OrderItem compareTo = (OrderItem)obj;

        if (getItemType() == null && compareTo.getItemType() != null)
        {
            return false;
        }
        else if (getItemType() != null && !getItemType().equals(compareTo.getItemType()))
        {
            return false;
        }

        if (getOrder() == null && compareTo.getOrder() != null)
        {
            return false;
        }
        else if (getOrder() != null && !getOrder().equals(compareTo.getOrder()))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        if (cachedHashCode != -1)
        {
            return cachedHashCode;
        }
        cachedHashCode = 17;
        if (getItemType() != null)
        {
            cachedHashCode = cachedHashCode * 23 + getItemType().hashCode();
        }
        else
        {
            cachedHashCode = cachedHashCode * 23;
        }
        if (getOrder() != null)
        {
            cachedHashCode = cachedHashCode * 23 + getOrder().hashCode();
        }
        else
        {
            cachedHashCode = cachedHashCode * 23;
        }

        canSetItemType = false;
        canSetOrder = false;
        return cachedHashCode;
    }

    public void delete()
    {
        itemType = null;
        order = null;
    }

}