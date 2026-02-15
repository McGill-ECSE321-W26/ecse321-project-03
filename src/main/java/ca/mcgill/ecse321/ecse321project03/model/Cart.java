package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 91 "model.ump"
// line 158 "model.ump"
public class Cart
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Cart Attributes
    private int id;

    //Cart Associations
    private Account account;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Cart(int aId, Account aAccount)
    {
        id = aId;
        if (aAccount == null || aAccount.getCart() != null)
        {
            throw new RuntimeException("Unable to create Cart due to aAccount. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        account = aAccount;
    }

    public Cart(int aId, int aIdForAccount, String aUsernameForAccount, String aEmailForAccount, String aPasswordForAccount, String aAddressForAccount, int aLoyaltyPtsForAccount)
    {
        id = aId;
        account = new Account(aIdForAccount, aUsernameForAccount, aEmailForAccount, aPasswordForAccount, aAddressForAccount, aLoyaltyPtsForAccount, this);
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
    /* Code from template association_GetOne */
    public Account getAccount()
    {
        return account;
    }

    public void delete()
    {
        Account existingAccount = account;
        account = null;
        if (existingAccount != null)
        {
            existingAccount.delete();
        }
    }


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "account = "+(getAccount()!=null?Integer.toHexString(System.identityHashCode(getAccount())):"null");
    }
}