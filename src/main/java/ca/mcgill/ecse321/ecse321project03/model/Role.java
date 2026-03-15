package ca.mcgill.ecse321.ecse321project03.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Role {

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Role Attribute
    @Id
    @GeneratedValue
    private int id;

    //Role Associations
    @OneToOne(optional = false)
    private Account account;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Role(Account aAccount) {
        if (!setAccount(aAccount)) {
            throw new RuntimeException("Unable to create Role due to aAccount. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    public Role() {
    }


    //------------------------
    // INTERFACE
    //------------------------

    // Setter method
    public boolean setAccount(Account aNewAccount) {
        boolean wasSet = false;
        if (aNewAccount != null) {
            account = aNewAccount;
            wasSet = true;
        }
        return wasSet;
    }

    // Getter methods
    public Account getAccount() {
        return account;
    }

    public int getId() {
        return id;
    }

    // Delete method
    public void delete() {
        account = null;
    }


}
