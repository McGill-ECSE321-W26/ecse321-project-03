package ca.mcgill.ecse321.ecse321project03.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Reference: Tutorial 7
public class AccountCreationDto {

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //displayed fields
    
    @NotBlank(message = "Username cannot be empty or null.")
    private String username;
    
    @Email
    @NotBlank(message = "Email cannot be empty or null.")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @NotBlank(message = "Password cannot be empty or null")
    private String password;

    @NotBlank(message = "Address cannot be empty or null.")
    private String address;


    //------------------------
    // CONSTRUCTOR
    //------------------------

    public AccountCreationDto(String aUsername, String aEmail, String aPassword, String aAddress) {
        username = aUsername;
        email = aEmail;
        password = aPassword;
        address = aAddress;
    }

    @SuppressWarnings("unused")
    public AccountCreationDto() {

    }

    //------------------------
    // INTERFACE
    //------------------------

    // Getter methods

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

}


