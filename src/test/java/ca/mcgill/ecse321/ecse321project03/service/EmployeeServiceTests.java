package ca.mcgill.ecse321.ecse321project03.service;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EmployeeServiceTests {

    //NOTE: I will only check for failure for one of the function as the all fail the same way
    //ie, they all call the same function that may cause potential errors

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private static final int MANAGER_ACCOUNT_ID = 1;
    private static final int EMPLOYEE_ACCOUNT_ID = 2;
    private static final int CUSTOMER_ACCOUNT_ID = 3;
    private static final int EMPLOYEE2_ACCOUNT_ID = 4;

    private Account managerAccount;
    private Manager manager;
    private Account customer;
    private Account employeeAccount;
    private Employee employee;
    private Account employeeAccount2;
    private Employee employee2;


    private List<Employee> employeeList;



    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        managerAccount = new Account("managerUser", "manager@mail.com", "passm", "addrm", 0);
        managerAccount.setId(MANAGER_ACCOUNT_ID);
        manager = new Manager(managerAccount);
        employeeAccount = new Account("employeeUser", "employee@mail.com", "passe", "addre", 0);
        employeeAccount.setId(EMPLOYEE_ACCOUNT_ID);
        employee = new Employee(employeeAccount);
        employeeAccount2 = new Account("employeeUser2", "employee2@mail.com", "passe2", "addre2", 0);
        employeeAccount2.setId(EMPLOYEE2_ACCOUNT_ID);
        employee2 = new Employee(employeeAccount2);
        customer = new Account("customerUser", "customer@mail.com", "passc", "addrc", 0);
        customer.setId(CUSTOMER_ACCOUNT_ID);



        employeeList = new ArrayList<>();
        employeeList.add(employee);
        employeeList.add(employee2);
    }




    //------------------------
    //View Employee tests
    //------------------------
    //success
    @Test
    public void viewEmployeesTest(){
        //arrange
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(employeeRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employee);
        when(employeeRepository.findAll()).thenReturn(employeeList);

        //act
        List<Employee> list = employeeService.viewEmployees(EMPLOYEE_ACCOUNT_ID);
        List <Employee> list2 = employeeService.viewEmployees(MANAGER_ACCOUNT_ID);

        //assert
        assertEquals(employeeList, list);
        assertEquals(employeeList, list2);
    }

    //failure
    @Test
    public void viewEmployeesByCustomer(){
        //arrange
        //nothing to arrange

        //act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> employeeService.viewEmployees(CUSTOMER_ACCOUNT_ID));

        //assert
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals( "Only manager and employees may view all employees in the system", e.getMessage());
    }
}