package ca.mcgill.ecse321.ecse321project03.model;

public abstract class Role {
    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Role Associations
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
}
