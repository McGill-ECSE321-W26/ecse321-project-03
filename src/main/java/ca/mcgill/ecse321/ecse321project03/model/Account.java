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


    //------------------------
    // CONSTRUCTOR
    //------------------------



    public Account(String aUsername, String aEmail, String aPassword, String aAddress, int aLoyaltyPts)
    {
        username = aUsername;
        email = aEmail;
        password = aPassword;
        address = aAddress;
        loyaltyPts = aLoyaltyPts;

    }

    public Account() {

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




    public void delete()
    {

    }


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "username" + ":" + getUsername()+ "," +
                "email" + ":" + getEmail()+ "," +
                "password" + ":" + getPassword()+ "," +
                "address" + ":" + getAddress()+ "," +
                "loyaltyPts" + ":" + getLoyaltyPts()+ "]";
    }
}


