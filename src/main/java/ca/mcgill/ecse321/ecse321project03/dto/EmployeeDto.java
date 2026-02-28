package ca.mcgill.ecse321.ecse321project03.dto;
import ca.mcgill.ecse321.ecse321project03.model.Employee;

// reference: tutorial 7
public class EmployeeDto {
    
    //displayed fields
    private int employeeId;
    private AccountResponseDto account;

    // constructor
    public EmployeeDto(Employee employee) {
        this.employeeId = employee.getAccount().getId();
        this.account = new AccountResponseDto(employee.getAccount());
    }

    @SuppressWarnings("unused")
    public EmployeeDto() {

    }

    // getter methods
    public int getEmployeeId() {
        return employeeId;
    }

    public AccountResponseDto getAccount() {
        return account;
    }

}
