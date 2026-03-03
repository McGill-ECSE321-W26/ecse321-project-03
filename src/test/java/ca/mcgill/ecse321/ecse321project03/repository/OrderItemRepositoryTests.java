package ca.mcgill.ecse321.ecse321project03.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.model.OrderItem.OrderItemId;


@SpringBootTest
public class OrderItemRepositoryTests {
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @AfterEach
    public void clearDatabase() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        itemTypeRepository.deleteAll();
        itemRepository.deleteAll();
        employeeRepository.deleteAll();
        roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadOrderItem() {
        // ---------- 1) Create Item + ItemType ----------
        Item item = new Item("Dress1", Item.Tag.Dress, 4.50);
        item = itemRepository.save(item);

        ItemType itemType = new ItemType(ItemType.Size.M, ItemType.Color.Black, 10, item);
        itemType = itemTypeRepository.save(itemType);

        // ---------- 2) Create Accounts ----------
        Account customer = new Account();
        customer.setUsername("customer1");
        customer.setEmail("customer1@test.com");
        customer.setPassword("pw");
        customer.setAddress("123 Test St");
        customer.setLoyaltyPts(10);
        customer = accountRepository.save(customer);

        Account employeeAccount = new Account();
        employeeAccount.setUsername("employee1");
        employeeAccount.setEmail("employee1@test.com");
        employeeAccount.setPassword("pw");
        employeeAccount.setAddress("456 Work Rd");
        employeeAccount.setLoyaltyPts(0);
        employeeAccount = accountRepository.save(employeeAccount);

        // ---------- 3) Create Employee (assignee) ----------
        Employee assignee = new Employee(employeeAccount);
        assignee = employeeRepository.save(assignee);

        // ---------- 4) Create Order ----------
        Date orderDate = Date.valueOf("2026-02-19");
        Date delivDate = Date.valueOf("2026-02-20");

        Order order = new Order(orderDate, delivDate, Order.OrderState.Ordered, customer, assignee);
        order = orderRepository.save(order);

        // ---------- 5) Create OrderItem (composite key) ----------
        int qty = 2;
        OrderItem.OrderItemId id = new OrderItem.OrderItemId(itemType, order);
        OrderItem orderItem = new OrderItem(id, qty);
        orderItem = orderItemRepository.save(orderItem);

        // ---------- 6) Load back & Test ----------
        Iterable<OrderItem> all = orderItemRepository.findAll();

        int count = 0;
        OrderItem orderItemFromDb = null;
        for (OrderItem oi : all) {
            count++;
            orderItemFromDb = oi;
        }
        assertEquals(1, count);
        assertNotNull(orderItemFromDb);

        OrderItemId idFromDb = orderItemFromDb.getOrderItemId();
        assertNotNull(idFromDb);

        // Check quantity
        assertEquals(qty, orderItemFromDb.getQuantity());

        // Check linked ItemType
        assertNotNull(idFromDb.getItemType());
        assertEquals(itemType.getId(), idFromDb.getItemType().getId());

        // Check linked Order
        assertNotNull(idFromDb.getOrder());
        assertEquals(order.getId(), idFromDb.getOrder().getId());

        // Check Order fields still match
        assertEquals(orderDate, idFromDb.getOrder().getOrderDate());
        assertEquals(delivDate, idFromDb.getOrder().getDelivDate());
        assertEquals(Order.OrderState.Ordered, idFromDb.getOrder().getOrderState());

        // Check Order customer
        assertNotNull(idFromDb.getOrder().getCustomer());
        assertEquals(customer.getId(), idFromDb.getOrder().getCustomer().getId());

        // Check Order assignee (Employee -> Account)
        assertNotNull(idFromDb.getOrder().getAssignee());
        assertNotNull(idFromDb.getOrder().getAssignee().getAccount());
        assertEquals(employeeAccount.getId(), idFromDb.getOrder().getAssignee().getAccount().getId());
    }
}
