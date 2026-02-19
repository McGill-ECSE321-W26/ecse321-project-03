package ca.mcgill.ecse321.ecse321project03.model;
/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 24 "model.ump"
// line 108 "model.ump"

import jakarta.persistence.*;

@Entity
@Inheritance (strategy = InheritanceType.JOINED)
public abstract class Role
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------
    @Id
    @GeneratedValue
    private int id;
    //Role Associations

    @OneToOne(optional = false)
    private Account account;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Role(Account aAccount)
    {
        if (!setAccount(aAccount))
        {
            throw new RuntimeException("Unable to create Role due to aAccount. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    protected Role() { }

    //------------------------
    // INTERFACE
    //------------------------
    /* Code from template association_GetOne */
    public Account getAccount()
    {
        return account;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setAccount(Account aNewAccount)
    {
        boolean wasSet = false;
        if (aNewAccount != null)
        {
            account = aNewAccount;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        account = null;
    }

    public int getId() {
        return id;
    }


}
