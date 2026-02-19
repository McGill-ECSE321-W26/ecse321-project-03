package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 91 "model.ump"
// line 158 "model.ump"

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Cart
{

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

    public Cart(Account aAccount)
    {
        if (!setAccount(aAccount))
        {
            throw new RuntimeException("Unable to create Cart due to aAccount. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    public Cart() {

    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setId(int aId)
    {
        boolean wasSet = false;
        id = aId;
        wasSet = true;
        return wasSet;
    }

    public int getId()
    {
        return id;
    }

    public Account getAccount()
    {
        return account;
    }
    /* Code from template association_GetOne */
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


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "account = "+(getAccount()!=null?Integer.toHexString(System.identityHashCode(getAccount())):"null");
    }
}