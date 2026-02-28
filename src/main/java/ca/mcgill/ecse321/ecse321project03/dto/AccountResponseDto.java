package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.Account;

//Reference: Tutoral 7
public class AccountResponseDto {



    //------------------------
    // MEMBER VARIABLES
    //------------------------


    // displayed fields (no password)
    private int id;
    private String username;
    private String email;
    private String address;
    private int loyaltyPts;


    //------------------------
    // CONSTRUCTOR
    //------------------------

    public AccountResponseDto(Account account) {
        id = account.getId();
        username = account.getUsername();
        email = account.getEmail();
        address = account.getAddress();
        loyaltyPts = account.getLoyaltyPts();
    }

    @SuppressWarnings("unused")
    public AccountResponseDto() {

    }

    //------------------------
    // INTERFACE
    //------------------------

    // Getter methods

    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getLoyaltyPts() {
        return loyaltyPts;
    }

    public String getAddress() {
        return address;
    }

}


