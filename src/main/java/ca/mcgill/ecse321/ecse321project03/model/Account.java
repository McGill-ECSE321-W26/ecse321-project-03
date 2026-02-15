package ca.mcgill.ecse321.ecse321project03.model;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.36.0.8091.03bcab5b3 modeling language!*/


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

// line 11 "model.ump"
// line 102 "model.ump"
@Entity
public class Account
{

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Account Attributes
    @Id
    @GeneratedValue
    private int id;
    private String username;
    private String email;
    private String password;
    private String address;
    private int loyaltyPts;

    //Account Associations

    @OneToOne
    private Cart cart;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Account(String aUsername, String aEmail, String aPassword, String aAddress, int aLoyaltyPts, Cart aCart)
    {
        username = aUsername;
        email = aEmail;
        password = aPassword;
        address = aAddress;
        loyaltyPts = aLoyaltyPts;
        if (aCart == null || aCart.getAccount() != null)
        {
            throw new RuntimeException("Unable to create Account due to aCart. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        cart = aCart;
    }

    public Account(int aId, String aUsername, String aEmail, String aPassword, String aAddress, int aLoyaltyPts)
    {
        id = aId;
        username = aUsername;
        email = aEmail;
        password = aPassword;
        address = aAddress;
        loyaltyPts = aLoyaltyPts;
        cart = new Cart(this);
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

    public boolean setUsername(String aUsername)
    {
        boolean wasSet = false;
        username = aUsername;
        wasSet = true;
        return wasSet;
    }

    public boolean setEmail(String aEmail)
    {
        boolean wasSet = false;
        email = aEmail;
        wasSet = true;
        return wasSet;
    }

    public boolean setPassword(String aPassword)
    {
        boolean wasSet = false;
        password = aPassword;
        wasSet = true;
        return wasSet;
    }

    public boolean setAddress(String aAddress)
    {
        boolean wasSet = false;
        address = aAddress;
        wasSet = true;
        return wasSet;
    }

    public boolean setLoyaltyPts(int aLoyaltyPts)
    {
        boolean wasSet = false;
        loyaltyPts = aLoyaltyPts;
        wasSet = true;
        return wasSet;
    }

    public int getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    public String getAddress()
    {
        return address;
    }

    public int getLoyaltyPts()
    {
        return loyaltyPts;
    }
    /* Code from template association_GetOne */
    public Cart getCart()
    {
        return cart;
    }

    public void delete()
    {
        Cart existingCart = cart;
        cart = null;
        if (existingCart != null)
        {
            existingCart.delete();
        }
    }


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "username" + ":" + getUsername()+ "," +
                "email" + ":" + getEmail()+ "," +
                "password" + ":" + getPassword()+ "," +
                "address" + ":" + getAddress()+ "," +
                "loyaltyPts" + ":" + getLoyaltyPts()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "cart = "+(getCart()!=null?Integer.toHexString(System.identityHashCode(getCart())):"null");
    }
}