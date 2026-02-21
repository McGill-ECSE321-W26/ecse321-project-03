package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Cart {

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Cart Attributes
    @Id
    @GeneratedValue
    private int id;

    //Cart Associations
    @OneToOne
    private Account account;


    //------------------------
    // CONSTRUCTOR
    //------------------------
    public Cart(Account aAccount) {
        if (!setAccount(aAccount)) {
            throw new RuntimeException("Unable to create Cart due to aAccount. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    public Cart() {

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

    public boolean setAccount(Account aNewAccount) {
        boolean wasSet = false;
        if (aNewAccount != null) {
            account = aNewAccount;
            wasSet = true;
        }
        return wasSet;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }


    // Delete method
    public void delete() {
        account = null;
    }


    // toString method
    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "account = " + (getAccount() != null ? Integer.toHexString(System.identityHashCode(getAccount())) : "null");
    }
}