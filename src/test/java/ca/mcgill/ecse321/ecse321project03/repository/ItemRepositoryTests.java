package ca.mcgill.ecse321.ecse321project03.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.ecse321project03.model.*;

@SpringBootTest
public class ItemRepositoryTests {
    @Autowired
    private ItemRepository itemRepository;

    @AfterEach
    public void clearDatabase() {
        itemRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadItem() {
        // ---------- 1) Create Item ----------
        Item item = new Item("Dress1", Item.Tag.Dress, 49.99);
        item = itemRepository.save(item);

        // ---------- 2) Check ID is generated ----------
        assertNotEquals(0, item.getId());
        int itemId = item.getId();

        // ---------- 3) Load back & Test ----------
        Item loaded = itemRepository.findById(itemId).orElse(null);
        assertNotNull(loaded);
        assertEquals(itemId, loaded.getId());
        assertEquals("Dress1", loaded.getName());
        assertEquals(Item.Tag.Dress, loaded.getTag());
        assertEquals(49.99, loaded.getPrice());
    }
}
