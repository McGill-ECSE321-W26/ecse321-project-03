package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 82 "model.ump"
// line 139 "model.ump"
public class CartItem
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //CartItem Attributes
    private int quantity;

    //CartItem Associations
    private ItemType itemType;
    private Cart cart;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public CartItem(int aQuantity, ItemType aItemType, Cart aCart)
    {
        quantity = aQuantity;
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
        if (aNewCart != null)
        {
            cart = aNewCart;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        itemType = null;
        cart = null;
    }


    public String toString()
    {
        return super.toString() + "["+
                "quantity" + ":" + getQuantity()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "itemType = "+(getItemType()!=null?Integer.toHexString(System.identityHashCode(getItemType())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "cart = "+(getCart()!=null?Integer.toHexString(System.identityHashCode(getCart())):"null");
    }
}
