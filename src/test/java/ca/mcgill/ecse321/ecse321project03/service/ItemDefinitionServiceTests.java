package ca.mcgill.ecse321.ecse321project03.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.ecse321project03.dto.ItemCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemCreationDto.VariantSpec;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.Item.Tag;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemTypeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderItemRepository;

@SpringBootTest
public class ItemDefinitionServiceTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemTypeRepository itemTypeRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ItemDefinitionService service;

    private static final int MANAGER_ACCOUNT_ID = 1;
    private Account managerAccount;
    private Manager manager;
    private ItemCreationDto validDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        managerAccount = new Account("managerUser", "manager@mail.com", "pass", "addr", 0);
        managerAccount.setId(MANAGER_ACCOUNT_ID);
        manager = new Manager(managerAccount);
        validDto = createValidItemCreationDto();
    }

    private ItemCreationDto createValidItemCreationDto() {
        ItemCreationDto dto = new ItemCreationDto();
        dto.setName("Classic Tee");
        dto.setTag("Top");
        dto.setPrice(BigDecimal.valueOf(19.99));
        List<VariantSpec> variants = new ArrayList<>();
        VariantSpec v1 = new VariantSpec();
        v1.setColor("Black");
        v1.setSize("S");
        v1.setInitialStock(10);
        variants.add(v1);
        dto.setVariants(variants);
        return dto;
    }

    // ---------- createItem: success ----------
    @Test
    public void testCreateValidItem() {
        // Optional<T> is the return type for default findById provided by Spring
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        // Return an empty list to pretend there are no items with replicated name
        when(itemRepository.findByName("Classic Tee")).thenReturn(new ArrayList<>());
        // Stubbing auto-generating an id for the item
        // Take the input item, change its id to 42, then return the item
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(42);
            return i;
        });
        // Stubbing saving and returning item type
        when(itemTypeRepository.save(any(ItemType.class))).thenAnswer(inv -> inv.getArgument(0));

        Item created = service.createItem(MANAGER_ACCOUNT_ID, validDto);

        assertNotNull(created);
        assertEquals("Classic Tee", created.getName());
        assertEquals(Tag.Top, created.getTag());
        assertEquals(19.99, created.getPrice());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(managerRepository, times(1)).findByAccount_Id(MANAGER_ACCOUNT_ID);
        verify(itemRepository, times(1)).findByName("Classic Tee");
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemTypeRepository, times(1)).save(any(ItemType.class));
    }

    // ---------- createItem: ensureManager failures ----------
    @Test
    public void testCreateItemUserNotLoggedIn() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(999, validDto));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        assertEquals("User not logged in.", e.getMessage());
    }

    @Test
    public void testCreateItemNotManager() {
        // Stubbing an existing account but this account is not a manager
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("Only managers can manage items.", e.getMessage());
    }

    // ---------- createItem: validateItemFields failures ----------
    @Test
    public void testCreateItemNullName() {
        // Re-used code is wrapped into private helper
        stubManagerAndItemNameUnique();
        validDto.setName(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("name: must not be null or blank.", e.getMessage());
    }

    @Test
    public void testCreateItemBlankName() {
        stubManagerAndItemNameUnique();
        validDto.setName("");

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("name: must not be null or blank.", e.getMessage());
    }

    @Test
    public void testCreateItemNameTooLong() {
        stubManagerAndItemNameUnique();
        validDto.setName("a".repeat(101));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("name: length must be between 1 and 100.", e.getMessage());
    }

    @Test
    public void testCreateItemInvalidTag() {
        stubManagerAndItemNameUnique();
        validDto.setTag("INVALID_TAG");

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("tag: must be a valid enum value.", e.getMessage());
    }

    @Test
    public void testCreateItemNullPrice() {
        stubManagerAndItemNameUnique();
        validDto.setPrice(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("price: must not be null.", e.getMessage());
    }

    @Test
    public void testCreateItemNegativePrice() {
        stubManagerAndItemNameUnique();
        validDto.setPrice(BigDecimal.valueOf(-1));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("price: must be >= 0.", e.getMessage());
    }

    // ---------- createItem: validateVariants failures ----------
    @Test
    public void testCreateItemVariantsNull() {
        stubManagerAndItemNameUnique();
        validDto.setVariants(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants: must not be null.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantsEmpty() {
        stubManagerAndItemNameUnique();
        validDto.setVariants(new ArrayList<>());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants: must not be empty.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantColorNull() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setColor(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].color: must not be null.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantColorBlank() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setColor("");

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].color: must not be null.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantInvalidColor() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setColor("NotAColor");

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].color: invalid value.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantSizeNull() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setSize(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].size: must not be null.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantSizeBlank() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setSize("");

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].size: must not be null.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantInvalidSize() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setSize("XXL");

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].size: invalid value.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantInitialStockNull() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setInitialStock(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].initialStock: must not be null.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantInitialStockNegative() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setInitialStock(-1);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].initialStock: must be >= 0.", e.getMessage());
    }

    @Test
    public void testCreateItemVariantInitialStockExceedsMax() {
        stubManagerAndItemNameUnique();
        validDto.getVariants().get(0).setInitialStock(100001);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("variants[0].initialStock: must be at most 100000.", e.getMessage());
    }

    @Test
    public void testCreateItemDuplicateVariant() {
        stubManagerAndItemNameUnique();
        VariantSpec v2 = new VariantSpec();
        v2.setColor("Black");
        v2.setSize("S");
        v2.setInitialStock(5);
        validDto.getVariants().add(v2);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("duplicate variant Black/S.", e.getMessage());
    }

    // ---------- createItem: checkItemNameUnique failure ----------
    @Test
    public void testCreateItemNameAlreadyExists() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        List<Item> existing = new ArrayList<>();
        existing.add(new Item("Classic Tee", Tag.Top, 10.0));
        // Pretend the same item already exists
        when(itemRepository.findByName(eq("Classic Tee"))).thenReturn(existing);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.createItem(MANAGER_ACCOUNT_ID, validDto));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("Item name already exists: Classic Tee", e.getMessage());
    }

    // ---------- getItemTypesForItem ----------
    @Test
    public void testGetItemTypesForItemSuccess() {
        int itemId = 1;
        Item item = new Item("Tee", Tag.Top, 19.99);
        item.setId(itemId);
        ItemType type1 = new ItemType(ItemType.Size.S, ItemType.Color.Black, 10, item);
        List<ItemType> types = List.of(type1);
        when(itemTypeRepository.findByGeneralItem_Id(itemId)).thenReturn(types);

        List<ItemType> result = service.getItemTypesForItem(itemId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ItemType.Size.S, result.get(0).getSize());
        assertEquals(ItemType.Color.Black, result.get(0).getColor());
        assertEquals(10, result.get(0).getStock());
        verify(itemTypeRepository, times(1)).findByGeneralItem_Id(itemId);
    }

    @Test
    public void testGetItemTypesForItemEmpty() {
        int itemId = 99;
        when(itemTypeRepository.findByGeneralItem_Id(itemId)).thenReturn(new ArrayList<>());

        List<ItemType> result = service.getItemTypesForItem(itemId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(itemTypeRepository, times(1)).findByGeneralItem_Id(itemId);
    }

    private void stubManagerAndItemNameUnique() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findByName(any(String.class))).thenReturn(new ArrayList<>());
    }

    // ---------- updateItem ----------
    @Test
    public void testUpdateItemSuccess() {
        int itemId = 5;
        Item existingItem = new Item("Classic Tee", Tag.Top, 19.99);
        existingItem.setId(itemId);
        ItemType existingType = new ItemType(ItemType.Size.S, ItemType.Color.Black, 5, existingItem);
        existingType.setId(100);

        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.findByName("Classic Tee")).thenReturn(List.of(existingItem));
        when(itemTypeRepository.findByGeneralItem_Id(itemId)).thenReturn(List.of(existingType));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemTypeRepository.save(any(ItemType.class))).thenAnswer(inv -> inv.getArgument(0));

        Item updated = service.updateItem(MANAGER_ACCOUNT_ID, itemId, validDto, null);

        assertNotNull(updated);
        assertEquals("Classic Tee", updated.getName());
        assertEquals(10, existingType.getStock());
        verify(itemTypeRepository, times(1)).save(existingType);
        verify(imageService, times(0)).replaceItemImages(anyInt(), anyInt(), any());
    }

    @Test
    public void testUpdateItemNotFound() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.updateItem(MANAGER_ACCOUNT_ID, 999, validDto, null));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    public void testUpdateItemNameConflictOtherItem() {
        int itemId = 5;
        Item existingItem = new Item("Classic Tee", Tag.Top, 19.99);
        existingItem.setId(itemId);
        Item other = new Item("Classic Tee", Tag.Dress, 1.0);
        other.setId(99);

        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.findByName("Classic Tee")).thenReturn(List.of(existingItem, other));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.updateItem(MANAGER_ACCOUNT_ID, itemId, validDto, null));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    // ---------- deleteItem ----------
    @Test
    public void testDeleteItemSuccess() {
        int itemId = 7;
        Item item = new Item("ToDelete", Tag.Top, 1.0);
        item.setId(itemId);

        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemTypeRepository.findByGeneralItem_Id(itemId)).thenReturn(new ArrayList<>());

        service.deleteItem(MANAGER_ACCOUNT_ID, itemId);

        verify(imageService, times(1)).deleteAllItemImages(MANAGER_ACCOUNT_ID, itemId);
        verify(itemRepository, times(1)).delete(item);
    }

    @Test
    public void testDeleteItemNotFound() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(888)).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.deleteItem(MANAGER_ACCOUNT_ID, 888));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
}
