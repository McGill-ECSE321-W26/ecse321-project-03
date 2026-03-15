package ca.mcgill.ecse321.ecse321project03.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.model.Item.Tag;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemTypeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;

@SpringBootTest
public class InventoryServiceTests {

    @Mock
    private ItemTypeRepository itemTypeRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private InventoryService service;

    private static final int MANAGER_ACCOUNT_ID = 1;
    private static final int ITEM_TYPE_ID = 5;
    private Account managerAccount;
    private Manager manager;
    private Item item;
    private ItemType itemType;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        managerAccount = new Account("managerUser", "manager@mail.com", "pass", "addr", 0);
        managerAccount.setId(MANAGER_ACCOUNT_ID);
        manager = new Manager(managerAccount);
        item = new Item("Test Item", Tag.Top, 19.99);
        item.setId(1);
        itemType = new ItemType(ItemType.Size.M, ItemType.Color.Black, 20, item);
        itemType.setId(ITEM_TYPE_ID);
    }

    // ---------- restockItemType: success ----------
    @Test
    public void testRestockItemTypeSuccess() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));
        when(itemTypeRepository.save(itemType)).thenReturn(itemType);

        ItemType result = service.restockItemType(ITEM_TYPE_ID, 10, MANAGER_ACCOUNT_ID);

        assertNotNull(result);
        assertEquals(30, result.getStock());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(roleRepository, times(2)).findByAccount_Id(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
        verify(itemTypeRepository, times(1)).save(itemType);
    }

    // ---------- restockItemType: failures ----------
    @Test
    public void testRestockItemTypeAccountNotFound() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.restockItemType(ITEM_TYPE_ID, 10, 999));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account managing the stock found with id 999", e.getMessage());
        verify(accountRepository, times(1)).findById(999);
    }

    @Test
    public void testRestockItemTypeCustomerCannotManage() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(null);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.restockItemType(ITEM_TYPE_ID, 10, MANAGER_ACCOUNT_ID));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("A customer cannot manage the stock", e.getMessage());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(roleRepository, times(1)).findByAccount_Id(MANAGER_ACCOUNT_ID);
    }

    @Test
    public void testRestockItemTypeEmployeeCannotManage() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        Employee employee = new Employee(managerAccount);
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(employee);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.restockItemType(ITEM_TYPE_ID, 10, MANAGER_ACCOUNT_ID));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("An employee cannot manage the stock", e.getMessage());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(roleRepository, times(2)).findByAccount_Id(MANAGER_ACCOUNT_ID);
    }

    @Test
    public void testRestockItemTypeQuantityNotPositive() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.restockItemType(ITEM_TYPE_ID, 0, MANAGER_ACCOUNT_ID));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Restock quantity must be positive", e.getMessage());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(roleRepository, times(2)).findByAccount_Id(MANAGER_ACCOUNT_ID);
    }

    @Test
    public void testRestockItemTypeQuantityNegative() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.restockItemType(ITEM_TYPE_ID, -5, MANAGER_ACCOUNT_ID));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Restock quantity must be positive", e.getMessage());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(roleRepository, times(2)).findByAccount_Id(MANAGER_ACCOUNT_ID);
    }

    @Test
    public void testRestockItemTypeItemTypeNotFound() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.restockItemType(ITEM_TYPE_ID, 10, MANAGER_ACCOUNT_ID));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No item type found with id " + ITEM_TYPE_ID, e.getMessage());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(roleRepository, times(2)).findByAccount_Id(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
    }

    // ---------- getItemTypeStockStatus: success ----------
    @Test
    public void testGetItemTypeStockStatusInStock() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));

        String result = service.getItemTypeStockStatus(ITEM_TYPE_ID, MANAGER_ACCOUNT_ID);

        assertEquals("In Stock", result);
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
    }

    @Test
    public void testGetItemTypeStockStatusAlmostOutOfStock() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        itemType.setStock(5);
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));

        String result = service.getItemTypeStockStatus(ITEM_TYPE_ID, MANAGER_ACCOUNT_ID);

        assertEquals("Almost Out of Stock! Only 5 left!!!", result);
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
    }

    @Test
    public void testGetItemTypeStockStatusOutOfStock() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        itemType.setStock(0);
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));

        String result = service.getItemTypeStockStatus(ITEM_TYPE_ID, MANAGER_ACCOUNT_ID);

        assertEquals("Out of Stock", result);
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
    }

    // ---------- getItemTypeStockStatus: failures ----------
    @Test
    public void testGetItemTypeStockStatusAccountNotFound() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.getItemTypeStockStatus(ITEM_TYPE_ID, 999));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with id 999", e.getMessage());
        verify(accountRepository, times(1)).findById(999);
    }

    @Test
    public void testGetItemTypeStockStatusItemTypeNotFound() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.getItemTypeStockStatus(ITEM_TYPE_ID, MANAGER_ACCOUNT_ID));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No item type found with id " + ITEM_TYPE_ID, e.getMessage());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
    }

    // ---------- getInventoryLevel: success ----------
    @Test
    public void testGetInventoryLevelSuccess() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));

        int result = service.getInventoryLevel(ITEM_TYPE_ID, MANAGER_ACCOUNT_ID);

        assertEquals(20, result);
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
    }

    // ---------- getInventoryLevel: failures ----------
    @Test
    public void testGetInventoryLevelAccountNotFound() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.getInventoryLevel(ITEM_TYPE_ID, 999));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account found with id 999", e.getMessage());
        verify(accountRepository, times(1)).findById(999);
    }

    @Test
    public void testGetInventoryLevelItemTypeNotFound() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.empty());

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.getInventoryLevel(ITEM_TYPE_ID, MANAGER_ACCOUNT_ID));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No item type found with id " + ITEM_TYPE_ID, e.getMessage());
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(itemTypeRepository, times(1)).findById(ITEM_TYPE_ID);
    }

    // ---------- getAllItemTypes: success ----------
    @Test
    public void testGetAllItemTypesSuccess() {
        List<ItemType> types = List.of(itemType);
        when(itemTypeRepository.findAll()).thenReturn(types);

        //List<ItemType> result = service.getAllItemTypes();

      //  assertNotNull(result);
       // assertEquals(1, result.size());
      //  assertEquals(ITEM_TYPE_ID, result.get(0).getId());
        verify(itemTypeRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllItemTypesEmpty() {
        when(itemTypeRepository.findAll()).thenReturn(new ArrayList<>());

       // List<ItemType> result = service.getAllItemTypes();

       // assertNotNull(result);
       // assertEquals(0, result.size());
        verify(itemTypeRepository, times(1)).findAll();
    }
}
