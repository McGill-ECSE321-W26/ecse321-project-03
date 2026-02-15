package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 83 "model.ump"
// line 151 "model.ump"
public class CartItem
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //CartItem Associations
    private ItemType itemType;
    private Cart cart;

    //Helper Variables
    private int cachedHashCode;
    private boolean canSetCart;
    private boolean canSetItemType;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public CartItem(ItemType aItemType, Cart aCart)
    {
        cachedHashCode = -1;
        canSetCart = true;
        canSetItemType = true;
        if (!setItemType(aItemType))
        {
            throw new RuntimeException("Unable to create CartItem due to aItemType. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setCart(aCart))
        {
            throw new RuntimeException("Unable to create CartItem due to aCart. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
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
    public Cart getCart()
    {
        return cart;
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
    public boolean setCart(Cart aNewCart)
    {
        boolean wasSet = false;
        if (!canSetCart) { return false; }
        if (aNewCart != null)
        {
            cart = aNewCart;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean equals(Object obj)
    {
        if (obj == null) { return false; }
        if (!getClass().equals(obj.getClass())) { return false; }

        CartItem compareTo = (CartItem)obj;

        if (getCart() == null && compareTo.getCart() != null)
        {
            return false;
        }
        else if (getCart() != null && !getCart().equals(compareTo.getCart()))
        {
            return false;
        }

        if (getItemType() == null && compareTo.getItemType() != null)
        {
            return false;
        }
        else if (getItemType() != null && !getItemType().equals(compareTo.getItemType()))
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
        if (getCart() != null)
        {
            cachedHashCode = cachedHashCode * 23 + getCart().hashCode();
        }
        else
        {
            cachedHashCode = cachedHashCode * 23;
        }
        if (getItemType() != null)
        {
            cachedHashCode = cachedHashCode * 23 + getItemType().hashCode();
        }
        else
        {
            cachedHashCode = cachedHashCode * 23;
        }

        canSetCart = false;
        canSetItemType = false;
        return cachedHashCode;
    }

    public void delete()
    {
        itemType = null;
        cart = null;
    }

}
