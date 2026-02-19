package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 73 "model.ump"
// line 145 "model.ump"

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Item
{

    //------------------------
    // ENUMERATIONS
    //------------------------

    public enum Tag { Top, Dress, Skirt, Pants, Accessory }

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Item Attributes
    private String name;
    private Tag tag;
    private double price;

    @Id
    @GeneratedValue
    private int id;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Item(String aName, Tag aTag, double aPrice)
    {
        name = aName;
        tag = aTag;
        price = aPrice;
    }

    public Item() {

    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setName(String aName)
    {
        boolean wasSet = false;
        name = aName;
        wasSet = true;
        return wasSet;
    }

    public boolean setTag(Tag aTag)
    {
        boolean wasSet = false;
        tag = aTag;
        wasSet = true;
        return wasSet;
    }

    public boolean setPrice(double aPrice)
    {
        boolean wasSet = false;
        price = aPrice;
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

    public String getName()
    {
        return name;
    }

    public Tag getTag()
    {
        return tag;
    }

    public double getPrice()
    {
        return price;
    }

    public int getId()
    {
        return id;
    }

    public void delete()
    {}


    public String toString()
    {
        return super.toString() + "["+
                "name" + ":" + getName()+ "," +
                "price" + ":" + getPrice()+ "," +
                "id" + ":" + getId()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "tag" + "=" + (getTag() != null ? !getTag().equals(this)  ? getTag().toString().replaceAll("  ","    ") : "this" : "null");
    }
}

