package ca.mcgill.ecse321.ecse321project03.persistance;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.*;

@SpringBootTest
public class ItemTypeRepositoryTests {
    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private ItemRepository itemRepository;

    @AfterEach
    public void clearDatabase() {
        // delete child first (so ItemType first)
        itemTypeRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadItemType() {
        // ---------- 1) Create Item ----------
        Item item = new Item("Dress1", Item.Tag.Dress, 49.99);
        item = itemRepository.save(item);
        assertNotEquals(0, item.getId());
        int itemId = item.getId();

        // ---------- 2) Create ItemType ----------
        ItemType itemType = new ItemType(ItemType.Size.M, ItemType.Color.Black, 10, item);
        itemType = itemTypeRepository.save(itemType);
        assertNotEquals(0, itemType.getId());
        int itemTypeId = itemType.getId();

        // ---------- 2) Load back & Test ----------
        ItemType loaded = itemTypeRepository.findById(itemTypeId).orElse(null);
        assertNotNull(loaded);

        // Check 4 attributes of ItemType
        assertEquals(ItemType.Size.M, loaded.getSize());
        assertEquals(ItemType.Color.Black, loaded.getColor());
        assertEquals(10, loaded.getStock());
        assertEquals(itemTypeId, loaded.getId());

        // Check Association: Item (general item)
        assertNotNull(loaded.getGeneralItem());
        assertEquals(itemId, loaded.getGeneralItem().getId());
    }
}
