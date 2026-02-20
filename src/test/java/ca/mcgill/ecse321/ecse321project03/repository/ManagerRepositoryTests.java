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
public class ManagerRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @AfterEach
    public void clearDatabase(){
        managerRepository.deleteAll();
        roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadManager(){
        // ---------- 1) Create + save account ----------
        String username = "ilovemuffins1234";
        String email = "muffinlove@thebest.com";
        String password = "Mu$fins";
        String address = "420 Bran Lane, Candyland, YourImagination";
        int lytPts = 67;
        Account muffinMan = new Account(username, email, password, address, lytPts);
        muffinMan = accountRepository.save(muffinMan);

        // ---------- 2) Create + save manager ----------
        Manager manager = new Manager();
        manager.setAccount(muffinMan);
        manager = managerRepository.save(manager);
        manager = roleRepository.save(manager);

        // ---------- 3) Check ID is generated ----------
        assertNotEquals(0, muffinMan.getId());
        assertNotEquals(0, manager.getId());
        int manID = manager.getId();

        // ---------- 4) Load back & Test ----------
        Manager muffinManager = managerRepository.findById(manID).orElse(null);
        Role muffinRole = roleRepository.findById(manID).orElse(null);
        assertNotNull(muffinManager);
        assertNotNull(muffinRole);
        assertEquals(muffinMan.getId(), muffinManager.getAccount().getId());
        assertEquals(manID, muffinRole.getId());
        assertEquals(manID, muffinManager.getId());

    }


}
