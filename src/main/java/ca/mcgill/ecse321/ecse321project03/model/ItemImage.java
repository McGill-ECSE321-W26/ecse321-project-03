package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Image associated with an item. Stored under ./uploads/; key is the logical path e.g. items/123/uuid.jpg.
 */
@Entity
public class ItemImage {

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    @Id
    @GeneratedValue
    private int id;
    private String key;

    @ManyToOne(optional = false)
    private Item item;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    protected ItemImage() {
    }

    public ItemImage(String aKey, Item aItem) {
        key = aKey;
        if (!setItem(aItem)) {
            throw new RuntimeException("Unable to create ItemImage due to aItem.");
        }
    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setId(int aId) {
        boolean wasSet = false;
        id = aId;
        wasSet = true;
        return wasSet;
    }

    public boolean setKey(String aKey) {
        boolean wasSet = false;
        key = aKey;
        wasSet = true;
        return wasSet;
    }

    public boolean setItem(Item aNewItem) {
        boolean wasSet = false;
        if (aNewItem != null) {
            item = aNewItem;
            wasSet = true;
        }
        return wasSet;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public Item getItem() {
        return item;
    }

    public void delete() {
        item = null;
    }

    @Override
    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "key" + ":" + getKey() + "]";
    }
}
