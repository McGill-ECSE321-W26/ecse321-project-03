package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 62 "model.ump"
// line 138 "model.ump"

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ItemType
{

    //------------------------
    // ENUMERATIONS
    //------------------------

    public enum Size { XS, S, M, L, XL }
    public enum Color { White, Brown, Black, Grey, Yellow, Green, Orange, Blue, Purple, Pink, Red }

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //ItemType Attributes
    private Size size;

    @Id
    @GeneratedValue
    private int id;
    private Color color;
    private int stock;

    //ItemType Associations
    @ManyToOne
    private Item generalItem;

    //------------------------
    // CONSTRUCTOR
    //------------------------
    protected ItemType() {}

    public ItemType(Size aSize, Color aColor, int aStock, Item aGeneralItem)
    {
        size = aSize;
        color = aColor;
        stock = aStock;
        if (!setGeneralItem(aGeneralItem))
        {
            throw new RuntimeException("Unable to create ItemType due to aGeneralItem. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    public ItemType() {

    }   

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setSize(Size aSize)
    {
        boolean wasSet = false;
        size = aSize;
        wasSet = true;
        return wasSet;
    }

    public boolean setId(int aId)
    {
        boolean wasSet = false;
        id = aId;
        wasSet = true;
        return wasSet;
    }

    public boolean setColor(Color aColor)
    {
        boolean wasSet = false;
        color = aColor;
        wasSet = true;
        return wasSet;
    }

    public boolean setStock(int aStock)
    {
        boolean wasSet = false;
        stock = aStock;
        wasSet = true;
        return wasSet;
    }

    public Size getSize()
    {
        return size;
    }

    public int getId()
    {
        return id;
    }

    public Color getColor()
    {
        return color;
    }

    public int getStock()
    {
        return stock;
    }
    /* Code from template association_GetOne */
    public Item getGeneralItem()
    {
        return generalItem;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setGeneralItem(Item aNewGeneralItem)
    {
        boolean wasSet = false;
        if (aNewGeneralItem != null)
        {
            generalItem = aNewGeneralItem;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        generalItem = null;
    }


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "stock" + ":" + getStock()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "size" + "=" + (getSize() != null ? !getSize().equals(this)  ? getSize().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "color" + "=" + (getColor() != null ? !getColor().equals(this)  ? getColor().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "generalItem = "+(getGeneralItem()!=null?Integer.toHexString(System.identityHashCode(getGeneralItem())):"null");
    }
}


