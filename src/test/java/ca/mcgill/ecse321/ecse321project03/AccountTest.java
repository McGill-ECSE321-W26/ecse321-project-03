package ca.mcgill.ecse321.ecse321project03;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountTest {

    @Autowired
    private Account account;

    @BeforeEach

    @AfterEach
    public void tearDown() {
        account = null;
    }

    @Test
    public void testSetUsername() {
        String newUsername = "newTestUser";
        assertTrue(account.setUsername(newUsername));
        assertEquals(newUsername, account.getUsername());
    }

    @Test
    public void testSetEmail() {
        String newEmail = "newTestEmail";
        assertTrue(account.setEmail(newEmail));
        assertEquals(newEmail, account.getEmail());
    }
}