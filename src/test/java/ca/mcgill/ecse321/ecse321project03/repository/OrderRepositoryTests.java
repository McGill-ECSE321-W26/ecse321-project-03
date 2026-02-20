package ca.mcgill.ecse321.ecse321project03.repository;

import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.Role;

import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.EmployeeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class OrderRepositoryTests {
    @Autowired private OrderRepository orderRepository;
    @Autowired private AccountRepository accountRepository;

    @Autowired(required = false) private RoleRepository roleRepository;
    @Autowired(required = false) private EmployeeRepository employeeRepository;

    @AfterEach
    public void clearDatabase() {
        // delete children first
        orderRepository.deleteAll();
        if (employeeRepository != null) employeeRepository.deleteAll();
        if (roleRepository != null) roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadOrderBasic() {

        // ---------- 1) Create + save customer account ----------
        Account customer = new Account();
        customer.setUsername("customer1");
        customer.setEmail("customer1@test.com");
        customer.setPassword("pw");
        customer.setAddress("123 Test St");
        customer.setLoyaltyPts(10);
        customer = accountRepository.save(customer);

        // ---------- 2) Create + save employee account ----------
        Account employeeAccount = new Account();
        employeeAccount.setUsername("employee1");
        employeeAccount.setEmail("employee1@test.com");
        employeeAccount.setPassword("pw");
        employeeAccount.setAddress("456 Work Rd");
        employeeAccount.setLoyaltyPts(0);
        employeeAccount = accountRepository.save(employeeAccount);

        // ---------- 3) Create + save employee role ----------
        Employee assignee = new Employee(employeeAccount);
        assignee = employeeRepository.save(assignee);

        // ---------- 4) Create + save order ----------
        Date orderDate = Date.valueOf("2026-02-19");
        Date delivDate = Date.valueOf("2026-02-20");
        Order.OrderState state = Order.OrderState.Ordered;
        Order order = new Order(orderDate, delivDate, state, customer, assignee);
        order = orderRepository.save(order);
        int orderId = order.getId();

        // ---------- 5) Load back & Test ----------
        Order orderFromDb = orderRepository.findById(orderId).orElse(null);
        assertNotNull(orderFromDb);
        // Check Order's Attributes
        assertEquals(orderDate, orderFromDb.getOrderDate());
        assertEquals(delivDate, orderFromDb.getDelivDate());
        assertEquals(state, orderFromDb.getOrderState());
        // Check Association: Customer Account
        assertNotNull(orderFromDb.getCustomer());
        assertEquals(customer.getId(), orderFromDb.getCustomer().getId());
        //Check Association: Employee
        assertNotNull(orderFromDb.getAssignee());
        assertEquals(assignee.getId(), orderFromDb.getAssignee().getId());
    }
}
