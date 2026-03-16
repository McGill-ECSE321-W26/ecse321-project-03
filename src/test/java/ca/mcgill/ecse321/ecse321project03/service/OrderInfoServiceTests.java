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
public class OrderInfoServiceTests {

    //NOTE: I will only check for failure for one of the function as the all fail the same way
    //ie, they all call the same function that may cause potential errors

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderInfoService orderInfoService;

    private static final int MANAGER_ACCOUNT_ID = 1;
    private static final int EMPLOYEE_ACCOUNT_ID = 2;
    private static final int CUSTOMER_ACCOUNT_ID = 3;
    private static final int ORDER_ID1 = 1;
    private static final int ORDER_ID2 = 2;
    private static final int ORDER_ID3 = 3;
    private Account managerAccount;
    private Manager manager;
    private Role managerRole;
    private Account customer;
    private Account employeeAccount;
    private Employee employee;
    private Role employeeRole;
    private List<Order> orderList;
    private Order order1;
    private Order order2;
    private Order order3;
    private static final Date orderDate = new Date(2026, 3, 14);
    private static final Date delivDate = new Date(2026, 3, 22);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        managerAccount = new Account("managerUser", "manager@mail.com", "passm", "addrm", 0);
        managerAccount.setId(MANAGER_ACCOUNT_ID);
        manager = new Manager(managerAccount);
        managerRole = new Manager(managerAccount);
        employeeAccount = new Account("employeeUser", "employee@mail.com", "passe", "addre", 0);
        employeeAccount.setId(EMPLOYEE_ACCOUNT_ID);
        employee = new Employee(employeeAccount);
        employeeRole = new Employee(employeeAccount);
        customer = new Account("customerUser", "customer@mail.com", "passc", "addrc", 0);
        customer.setId(CUSTOMER_ACCOUNT_ID);

        //orders:
        //order1 and order3 -> same state
        //order2 and order3 -> same customer
        order1 = new Order(orderDate, delivDate, Order.OrderState.InPreparation, employeeAccount, employee);
        order1.setId(ORDER_ID1);
        order2 = new Order(delivDate, delivDate, Order.OrderState.Delivered, customer, employee);
        order2.setId(ORDER_ID2);
        order3 = new Order(new Date(System.currentTimeMillis()), delivDate, Order.OrderState.Ordered, managerAccount, employee);
        order3.setId(ORDER_ID3);
        orderList = new ArrayList<>();
        orderList.add(order1);
        orderList.add(order2);
        orderList.add(order3);
    }


    //------------------------
    //View Orders tests
    //------------------------
    //success
    @Test
    public void viewOrdersTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(managerRole);
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findAll()).thenReturn(orderList);

        //act
        List <Order> list = orderInfoService.viewOrders(EMPLOYEE_ACCOUNT_ID);
        List <Order> list2 = orderInfoService.viewOrders(MANAGER_ACCOUNT_ID);

        //assert
        assertEquals(orderList, list);
        assertEquals(orderList, list2);
    }

    //failure
    @Test
    public void viewOrdersNotEmployee(){
        //arrange
        //nothing to arrange

        //act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderInfoService.viewOrders(CUSTOMER_ACCOUNT_ID));

        //assert
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals( "Customers may not view of all of the orders in the system", e.getMessage());
    }

    //------------------------
    //View Orders tests
    //------------------------
    //success
    @Test
    public void viewOrdersByOrderDateAscendingTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(managerRole);
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findAll()).thenReturn(orderList);
        List <Order> ascList = new ArrayList<>();
        ascList.add(order3);
        ascList.add(order1);
        ascList.add(order2);
        List <Order> descList = new ArrayList<>();
        descList.add(order2);
        descList.add(order1);
        descList.add(order3);

        //act
        List <Order> list = orderInfoService.viewOrdersByOrderDate(EMPLOYEE_ACCOUNT_ID, "ascending");
        //assert
        assertEquals(ascList, list);

        //act
        list = orderInfoService.viewOrdersByOrderDate(MANAGER_ACCOUNT_ID, "descending");
        //assert
        assertEquals(descList, list);
    }


    //------------------------
    //View Orders by Customer tests
    //------------------------
    //success
    @Test
    public void viewOrdersByCustomerTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(managerRole);
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findAll()).thenReturn(orderList);
        List <Order> ascList = new ArrayList<>();
        ascList.add(order3);
        ascList.add(order1);
        ascList.add(order2);
        List <Order> descList = new ArrayList<>();
        descList.add(order2);
        descList.add(order1);
        descList.add(order3);

        //act
        List <Order> list = orderInfoService.viewOrdersByOrderDate(EMPLOYEE_ACCOUNT_ID, "ascending");
        //assert
        assertEquals(ascList, list);

        //act
        list = orderInfoService.viewOrdersByOrderDate(MANAGER_ACCOUNT_ID, "descending");
        //assert
        assertEquals(descList, list);
    }


    //------------------------
    //View Orders byt status tests
    //------------------------
    //success
    @Test
    public void viewOrdersByStatusTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(managerRole);
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findAll()).thenReturn(orderList);
        List <Order> ascList = new ArrayList<>();
        ascList.add(order3);
        ascList.add(order1);
        ascList.add(order2);
        List <Order> descList = new ArrayList<>();
        descList.add(order2);
        descList.add(order1);
        descList.add(order3);

        //act
        List <Order> list = orderInfoService.viewOrdersByOrderDate(EMPLOYEE_ACCOUNT_ID, "ascending");
        //assert
        assertEquals(ascList, list);

        //act
        list = orderInfoService.viewOrdersByOrderDate(MANAGER_ACCOUNT_ID, "descending");
        //assert
        assertEquals(descList, list);
    }

}
