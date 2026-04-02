package ca.mcgill.ecse321.ecse321project03.dto;
import ca.mcgill.ecse321.ecse321project03.model.Manager;


// reference: tutorial 7
public class ManagerDto {
    
    //displayed fields
    private int employeeId;
    private AccountResponseDto account;

    // constructor
    public ManagerDto(Manager manager) {
        this.employeeId = manager.getAccount().getId();
        this.account = new AccountResponseDto(manager.getAccount(), manager);
    }

    @SuppressWarnings("unused")
    public ManagerDto() {

    }

    // getter methods
    public int getEmployeeId() {
        return employeeId;
    }

    public AccountResponseDto getAccount() {
        return account;
    }

}