package ca.mcgill.ecse321.ecse321project03.repository;

import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class EmployeeRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @AfterEach
    public void clearDatabase(){
        employeeRepository.deleteAll();
        roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadEmployee(){
        // ---------- 1) Create + save account ----------
        String username = "ilovemuffins1234";
        String email = "muffinlove@thebest.com";
        String password = "Mu$fins";
        String address = "420 Bran Lane, Candyland, YourImagination";
        int lytPts = 67;
        Account muffinMan = new Account(username, email, password, address, lytPts);
        muffinMan = accountRepository.save(muffinMan);

        // ---------- 2) Create + save employee ----------
        Employee employee = new Employee();
        employee.setAccount(muffinMan);
        employee = employeeRepository.save(employee);
        employee = roleRepository.save(employee);

        // ---------- 3) Check ID is generated ----------
        assertNotEquals(0, muffinMan.getId());
        assertNotEquals(0, employee);
        int empID = employee.getId();

        // ---------- 4) Load back & Test ----------
        Employee muffinEmployee = employeeRepository.findById(empID).orElse(null);
        Role muffinRole = roleRepository.findById(empID).orElse(null);
        assertNotNull(muffinEmployee);
        assertNotNull(muffinRole);
        assertEquals(muffinMan.getId(), muffinEmployee.getAccount().getId());
        assertEquals(empID, muffinRole.getId());
        assertEquals(empID, muffinEmployee.getId());

    }

}
