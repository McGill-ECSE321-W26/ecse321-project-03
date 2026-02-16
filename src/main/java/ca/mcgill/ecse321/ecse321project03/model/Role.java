package ca.mcgill.ecse321.ecse321project03.model;
/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/



// line 24 "model.ump"
// line 108 "model.ump"

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;

@Entity
@Inheritance (strategy = InheritanceType.JOINED)
public abstract class Role
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Role Associations

    @OneToOne(optional = false)
    private Account account;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Role(Account aAccount)
    {
        boolean didAddAccount = setAccount(aAccount);
        if (!didAddAccount)
        {
            throw new RuntimeException("Unable to create role due to account. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
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
        if (aNewAccount == null)
        {
            //Unable to setAccount to null, as role must always be associated to a account
            return wasSet;
        }

        Role existingRole = aNewAccount.getRole();
        if (existingRole != null && !equals(existingRole))
        {
            //Unable to setAccount, the current account already has a role, which would be orphaned if it were re-assigned
            return wasSet;
        }

        Account anOldAccount = account;
        account = aNewAccount;
        account.setRole(this);

        if (anOldAccount != null)
        {
            anOldAccount.setRole(null);
        }
        wasSet = true;
        return wasSet;
    }

    public void delete()
    {
        Account existingAccount = account;
        account = null;
        if (existingAccount != null)
        {
            existingAccount.setRole(null);
        }
    }

}
