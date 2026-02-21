package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Item {

    //------------------------
    // ENUMERATIONS
    //------------------------

    public enum Tag {Top, Dress, Skirt, Pants, Accessory}

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
    protected Item() {
    }

    public Item(String aName, Tag aTag, double aPrice) {
        name = aName;
        tag = aTag;
        price = aPrice;
    }

    //------------------------
    // INTERFACE
    //------------------------

    // Setter methods
    public boolean setName(String aName) {
        boolean wasSet = false;
        name = aName;
        wasSet = true;
        return wasSet;
    }

    public boolean setTag(Tag aTag) {
        boolean wasSet = false;
        tag = aTag;
        wasSet = true;
        return wasSet;
    }

    public boolean setPrice(double aPrice) {
        boolean wasSet = false;
        price = aPrice;
        wasSet = true;
        return wasSet;
    }

    public boolean setId(int aId) {
        boolean wasSet = false;
        id = aId;
        wasSet = true;
        return wasSet;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public Tag getTag() {
        return tag;
    }

    public double getPrice() {
        return price;
    }

    public int getId() {
        return id;
    }


    // Delete method
    public void delete() {
    }

    // toString method
    public String toString() {
        return super.toString() + "[" +
                "name" + ":" + getName() + "," +
                "price" + ":" + getPrice() + "," +
                "id" + ":" + getId() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "tag" + "=" + (getTag() != null ? !getTag().equals(this) ? getTag().toString().replaceAll("  ", "    ") : "this" : "null");
    }
}

