package ca.mcgill.ecse321.ecse321project03.repository;

import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RoleRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CartRepository cartRepository;

    @AfterEach
    public void clearDatabase() {
        roleRepository.deleteAll();
        cartRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadRole() {
        // ---------- 1) Create + save accounts ----------
        String username = "ilovemuffins1234";
        String email = "muffinlove@thebest.com";
        String password = "Mu$fins";
        String address = "420 Bran Lane, Candyland, YourImagination";
        int lytPts = 67;
        Account muffinMan = new Account(username, email, password, address, lytPts);
        muffinMan = accountRepository.save(muffinMan);

        String username1 = "ilovemuffins12341";
        String email1 = "muffinlove@thebest.com1";
        String password1 = "Mu$fins1";
        String address1 = "420 Bran Lane, Candyland, YourImagination1";
        int lytPts1 = 69;
        Account muffinWoman = new Account(username1, email1, password1, address1, lytPts1);
        muffinWoman = accountRepository.save(muffinWoman);

        // ---------- 2) Create + save roles ----------
        Manager manager = new Manager();
        manager.setAccount(muffinWoman);
        manager = roleRepository.save(manager);

        Employee employee = new Employee();
        employee.setAccount(muffinMan);
        employee = roleRepository.save(employee);

        // ---------- 3) Check ID is generated ----------
        assertNotEquals(0, muffinWoman.getId());
        assertNotEquals(0, muffinMan.getId());
        assertNotEquals(0, manager.getId());
        assertNotEquals(0, employee.getId());
        int manID = manager.getId();
        int empID = employee.getId();

        // ---------- 4) Load back & Test ----------
        Role muffinManager = roleRepository.findById(manID).orElse(null);
        Role muffinEmployee = roleRepository.findById(empID).orElse(null);
        assertNotNull(muffinManager);
        assertNotNull(muffinEmployee);
        assertEquals(muffinWoman.getId(), muffinManager.getAccount().getId());
        assertEquals(muffinMan.getId(), muffinEmployee.getAccount().getId());
        assertEquals(manID, muffinManager.getId());
        assertEquals(empID, muffinEmployee.getId());
    }
}
