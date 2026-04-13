package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.model.Role;

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
    /** CUSTOMER, MANAGER, or EMPLOYEE */
    private String accountType;
    private boolean active;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public AccountResponseDto(Account account) {
        this(account, null);
    }

    public AccountResponseDto(Account account, Role role) {
        id = account.getId();
        username = account.getUsername();
        email = account.getEmail();
        address = account.getAddress();
        loyaltyPts = account.getLoyaltyPts();
        accountType = resolveAccountType(role);
        active = account.getActive();
    }

    @SuppressWarnings("unused")
    public AccountResponseDto() {

    }

    private static String resolveAccountType(Role role) {
        if (role == null) {
            return "CUSTOMER";
        }
        if (role instanceof Manager) {
            return "MANAGER";
        }
        if (role instanceof Employee) {
            return "EMPLOYEE";
        }
        return "CUSTOMER";
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

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

}


