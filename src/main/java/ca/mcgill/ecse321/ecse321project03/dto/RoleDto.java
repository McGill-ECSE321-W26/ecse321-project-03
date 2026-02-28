package ca.mcgill.ecse321.ecse321project03.dto;

// reference: tutorial 7
public class RoleDto {
    
    //displayed fields
    private int employeeId;
    private AccountResponseDto account;

    // constructor
    public RoleDto(int aEmployeeId, AccountResponseDto aAccount) {
        employeeId = aEmployeeId;
        account = aAccount;
    }

    @SuppressWarnings("unused")
    public RoleDto() {

    }

    // getter methods
    public int getEmployeeId() {
        return employeeId;
    }

    public AccountResponseDto getAccount() {
        return account;
    }

}