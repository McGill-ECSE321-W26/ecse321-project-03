package ca.mcgill.ecse321.ecse321project03.service;


import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceTests {

    //NOTE: some helper functions, albeit public, are simply calling the repository functions that we have already tested
    //As such, I will not be testing them in this class

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ItemTypeRepository itemTypeRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    private static final int MANAGER_ACCOUNT_ID = 1;
    private static final int EMPLOYEE_ACCOUNT_ID = 2;
    private static final int CUSTOMER_ACCOUNT_ID = 3;
    private static final int EMPLOYEE_ACCOUNT_ID2 = 4;
    private static final int ORDER_ID = 1;
    private static final int ORDER_ID_DUMMY = 2;
    private Account managerAccount;
    private Manager manager;
    private Role managerRole;
    private Account customer;
    private Account employeeAccount;
    private Employee employee;
    private Role employeeRole;
    private Account employeeAccount2;
    private Employee employee2;
    private Role employeeRole2;
    private Order order;
    private Order orderDummy;
    private Cart cart;
    private Item item;
    private ItemType itemType;
    private CartItem cartItem;
    private CartItem.CartItemId cartItemId;
    private OrderItem orderItem;
    private OrderItem.OrderItemId orderItemId;
    private static final Date orderDate = new Date(2026, 3, 14);
    private static final Date delivDate = new Date(2026, 3, 22);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        managerAccount = new Account("managerUser", "manager@mail.com", "passm", "addrm", 0);
        managerAccount.setId(MANAGER_ACCOUNT_ID);
        manager = new Manager(managerAccount);
        managerRole = new Manager(managerAccount);
        employeeAccount = new Account("employeeUser", "employee@mail.com", "passe", "addre", 0);
        employeeAccount.setId(EMPLOYEE_ACCOUNT_ID);
        employee = new Employee(employeeAccount);
        employeeRole = new Employee(employeeAccount);
        employeeAccount2 = new Account("employeeUser2", "employee2@mail.com", "passe", "addre", 0);
        employeeAccount2.setId(EMPLOYEE_ACCOUNT_ID2);
        employee2 = new Employee(employeeAccount2);
        employeeRole2 = new Employee(employeeAccount2);
        customer = new Account("customerUser", "customer@mail.com", "passc", "addrc", 0);
        customer.setId(CUSTOMER_ACCOUNT_ID);
        order = new Order(orderDate, delivDate, Order.OrderState.Ordered, customer, employee);
        order.setId(ORDER_ID);
        orderDummy = new Order(orderDate, delivDate, Order.OrderState.Ordered, employeeAccount, employee);
        orderDummy.setId(ORDER_ID_DUMMY);
        cart = new Cart(customer);
        item = new Item("Shirt", Item.Tag.Top, 15);
        itemType = new ItemType(ItemType.Size.M, ItemType.Color.White, 2, item);
        cartItemId = new CartItem.CartItemId(itemType, cart);
        cartItem = new CartItem(cartItemId, 2);
        orderItemId = new OrderItem.OrderItemId(itemType, order);
        orderItem = new OrderItem(orderItemId, cartItem.getQuantity());
    }



    //------------------------
    //Create Order tests
    //------------------------
    //success
    @Test
    public void createOrderTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), delivDate);
        Order newOrder = new Order(new Date(System.currentTimeMillis()), orderDto.getDelivDate(), Order.OrderState.Ordered, fakePerson, null);
        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        //act
        Order fakeOrder = orderService.createOrder(orderDto);

        //assert
        assertEquals(orderDto.getCustomerId(), fakeOrder.getCustomer().getId());
        assertEquals(orderDto.getDelivDate(), fakeOrder.getDelivDate());
    }

    //failure
    @Test
    public void createOrderWrongDelivDate(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), new Date(System.currentTimeMillis()));
        Order newOrder = new Order(new Date(System.currentTimeMillis()), orderDto.getDelivDate(), Order.OrderState.Ordered, fakePerson, null);
        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        //act - deliv date < 24 hours after order date
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.createOrder(orderDto));

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "The delivery date can not be placed less than 24 hours after the order date", e.getMessage());
    }


    //------------------------
    //Checkout tests
    //------------------------
    //success
    @Test
    public void checkoutTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(cartRepository.findByAccount_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(cart);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);
        when(cartItemRepository.findByCartItemId_Cart(cart)).thenReturn(List.of(cartItem));
        List<CartItem> ci = cartItemRepository.findByCartItemId_Cart(cart);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(itemTypeRepository.save(any(ItemType.class))).thenReturn(itemType);
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), orderDate);

        //act
        List <OrderItem> oi = orderService.checkout(orderDto);
        OrderItem ordIt = oi.getFirst();

        //assert
        assertEquals(1, oi.size());
        assertEquals(itemType, ordIt.getOrderItemId().getItemType());
        assertEquals(cartItem.getQuantity(), ordIt.getQuantity());
        assertEquals(customer, ordIt.getOrderItemId().getOrder().getCustomer());
        assertEquals(customer.getId(), ordIt.getOrderItemId().getOrder().getCustomer().getId());
        verify(cartItemRepository, times(1)).deleteAll(ci);
    }

    //failure
    @Test
    public void checkoutNoCustomer(){
        //arrange
        Account fakePerson = new Account("customerUserFake", "customerFake@mail.com", "passc", "addrc", 0);
        fakePerson.setId(10);
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), delivDate);
        Order newOrder = new Order(new Date(System.currentTimeMillis()), orderDto.getDelivDate(), Order.OrderState.Ordered, fakePerson, null);
        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        //act
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.checkout(orderDto));

        //assert
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "No cart found for customer with id "  + 10, e.getMessage());
    }
    

    @Test
    public void checkoutCustomerNoCart(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(cartRepository.findByAccount_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(null);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), orderDate);

        //act
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.checkout(orderDto));

        //assert
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "No cart found for customer with id " + CUSTOMER_ACCOUNT_ID, e.getMessage());
    }

    @Test
    public void checkoutEmptyCart(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        Cart fakeCart = new Cart(customer);
        when(cartRepository.findByAccount_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(fakeCart);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), orderDate);

        //act
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.checkout(orderDto));

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Cannot checkout with an empty cart", e.getMessage());
    }

    @Test
    public void checkoutInvalidQuantity(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(cartRepository.findByAccount_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(cart);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);
        CartItem cartItem1 = cartItem;
        cartItem1.setQuantity(0);
        when(cartItemRepository.findByCartItemId_Cart(cart)).thenReturn(List.of(cartItem1));
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), orderDate);

        //act
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.checkout(orderDto));

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Cart contains an invalid quantity: " + cartItem1.getQuantity(), e.getMessage());
    }

    @Test
    public void checkoutNoType(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(cartRepository.findByAccount_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(cart);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);
        CartItem.CartItemId ciid = new CartItem.CartItemId(null, cart);
        CartItem cartItem1 = new CartItem(ciid, 2);
        when(cartItemRepository.findByCartItemId_Cart(cart)).thenReturn(List.of(cartItem1));
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), orderDate);

        //act
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.checkout(orderDto));

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Cart contains an item with no item type", e.getMessage());
    }

    @Test
    public void checkoutNotEnoughStock(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(cartRepository.findByAccount_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(cart);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);
        CartItem cartItem1 = cartItem;
        cartItem1.getCartItemId().getItemType().setStock(0);
        when(cartItemRepository.findByCartItemId_Cart(cart)).thenReturn(List.of(cartItem1));
        OrderCreationDto orderDto = new OrderCreationDto(fakePerson.getId(), orderDate);

        //act
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.checkout(orderDto));

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Not enough stock for itemType id " + itemType.getId() +
                ". Requested " + cartItem1.getQuantity() + ", available " + itemType.getStock(), e.getMessage());
    }


    //------------------------
    //Assign Employee tests
    //------------------------
    //success
    @Test
    public void assignEmployee(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        Employee fakeEmployee = employee;
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = order;
        fakeOrder.setAssignee(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);


        //act
        Order newOrder = orderService.assignEmployee(fakeOrder.getId(), fakeEmployee.getId(), fakeEmployee.getAccount().getId());

        //assert
        assertEquals(fakeOrder.getId(), newOrder.getId());
        assertEquals(fakeEmployee.getId(), newOrder.getAssignee().getId());
    }

    //failure
    @Test
    public void assignEmployeeAccountDoesntExist(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        Account fakeEmployeeAcc = accountRepository.findById(EMPLOYEE_ACCOUNT_ID).orElse(null);
        when(employeeRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employee));
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        fakeOrder.setAssignee(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act - set accountId as an account that doesnt exist
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.assignEmployee(fakeOrder.getId(), fakeEmployeeAcc.getId(), 10));

        //assert
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "No account doing the action found with id " + 10, e.getMessage());
    }

    @Test
    public void assignEmployeeCustomerIsMakingChange(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        Account fakeEmployeeAcc = accountRepository.findById(EMPLOYEE_ACCOUNT_ID).orElse(null);
        when(employeeRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employee));
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        fakeOrder.setAssignee(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act - person assigning is not an employee
        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> orderService.assignEmployee(fakeOrder.getId(), fakeEmployeeAcc.getId(), fakePerson.getId()));

        //assert
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals( "The order cannot be assigned to the employee with id " + fakeEmployeeAcc.getId() + " as a customer is making the change.", e.getMessage());
    }

    @Test
    public void assignEmployeeEmployeeDoesntExist(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = customer;
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        Account fakeEmployeeAcc = employeeAccount;
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = order;
        fakeOrder.setAssignee(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act - employee does not exist
        fakeOrder.setCustomer(fakeEmployeeAcc);
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.assignEmployee(fakeOrder.getId(), fakePerson.getId(), employeeAccount.getId()));

        //assert
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "No employee found with id "+ fakePerson.getId(), e.getMessage());
    }

    @Test
    public void assignEmployeeEmployeeIsCustomer(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        Account fakeEmployeeAcc = employeeAccount;
        // Account fakeEmployeeAcc = accountRepository.findById(EMPLOYEE_ACCOUNT_ID).orElse(null);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        Employee fakeEmployee = employee;
        //Employee fakeEmployee = employeeRepository.findById(EMPLOYEE_ACCOUNT_ID).orElse(null);
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = order;
        //Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        fakeOrder.setAssignee(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act - employee is customer
        fakeOrder.setCustomer(fakeEmployeeAcc);
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.assignEmployee(fakeOrder.getId(), fakeEmployee.getId(), employeeAccount.getId()));

        //assert
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals( "The order cannot be assigned to the employee with id " + fakeEmployee.getId() + " as he is the customer.", e.getMessage());
    }

    @Test
    public void assignEmployeeAccountIsNotTheSameAsEmployee(){
        //arrange
        Role mockEmployeeRole = spy(employeeRole2);
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID2)).thenReturn(Optional.of(employeeAccount2));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        //when(employeeRepository.findById(employee2.getId())).thenReturn(Optional.of(employee2));
        Employee fakeEmployee = employee;
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);

        //when(mockEmployeeRole.getAccount()).thenReturn(employeeAccount2);
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID2)).thenReturn(mockEmployeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(mockEmployeeRole.getId()).thenReturn(1);
        Order fakeOrder = order;
        fakeOrder.setAssignee(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act - account is not employee
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.assignEmployee(fakeOrder.getId(), employee.getId(), mockEmployeeRole.getAccount().getId()));

        //assert
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals( "The order cannot be assigned to the employee by another employee.", e.getMessage());

    }


    //------------------------
    //Verify Update Status -> private helper function, no testing
    //------------------------


    //------------------------
    //Cancel Order -> private helper function, no testing
    //------------------------


    //------------------------
    //Update Status tests
    //------------------------
    //success
    @Test
    public void updateStatus(){
        //arrange
        //possible states {Ordered, InPreparation, Prepared, Delivered, Cancelled}
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(manager));
        when(roleRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(managerRole);
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        when(employeeRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employee));
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act - update status to InPreparation
        Order newOrder = orderService.updateStatus(fakeOrder.getId(), "InPreparation", employeeAccount.getId());
        //assert
        assertEquals(fakeOrder.getId(), newOrder.getId());
        assertEquals(Order.OrderState.InPreparation, newOrder.getOrderState());

        //act - update status to Prepared
        newOrder = orderService.updateStatus(fakeOrder.getId(), "Prepared", employeeAccount.getId());
        //assert
        assertEquals(fakeOrder.getId(), newOrder.getId());
        assertEquals(Order.OrderState.Prepared, newOrder.getOrderState());

        //act - update status to Prepared
        newOrder = orderService.updateStatus(fakeOrder.getId(), "Delivered", managerAccount.getId());
        //assert
        assertEquals(fakeOrder.getId(), newOrder.getId());
        assertEquals(Order.OrderState.Delivered, newOrder.getOrderState());

        //act - cancel order
        newOrder = orderService.updateStatus(fakeOrder.getId(), "Cancelled", managerAccount.getId());
        //assert
        assertEquals(fakeOrder.getId(), newOrder.getId());
    }

    //failure
    @Test
    public void updateStatusOrderNotFound(){
        //arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = order;
        fakeOrder.setId(2);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.updateStatus(fakeOrder.getId(), "Delivered", employee.getId()));

        //assert
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "No order found with id " +fakeOrder.getId(), e.getMessage());
    }

    @Test
    public void updateStatusNotEmployeeOrManager(){
        //arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = order;
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.updateStatus(fakeOrder.getId(), "InPreparation", customer.getId()));

        //assert
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals( "The order state cannot be updated by a customer", e.getMessage());
    }

    @Test
    public void updateStatusWrongState(){
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.updateStatus(fakeOrder.getId(), "Delivered", employeeAccount.getId()));

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Order with id " + fakeOrder.getId() + " cannot be switched to " + "Delivered" + " because it is not Prepared", e.getMessage());
    }

    @Test
    public void updateStatusWrongCancel(){
        when(accountRepository.findById(EMPLOYEE_ACCOUNT_ID)).thenReturn(Optional.of(employeeAccount));
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(roleRepository.findByAccount_Id(EMPLOYEE_ACCOUNT_ID)).thenReturn(employeeRole);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.updateStatus(fakeOrder.getId(), "Cancelled", employeeAccount.getId()));

        //assert
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals( "The order cannot be cancelled by an employee", e.getMessage());
    }

    @Test
    public void updateStatusWrongCancelCustomer(){
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Order fakeOrder = order;
        Date date = new Date(System.currentTimeMillis());
        fakeOrder.setDelivDate(date);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(fakeOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(fakeOrder);

        //act
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> orderService.updateStatus(fakeOrder.getId(), "Cancelled", customer.getId()));

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "The order cannot be cancelled less than 24 hours before the delivery date", e.getMessage());
    }

    @Test
    public void testAutomaticDeliveryUpdate(){
        // ordered 2 days ago, delivery scheduled to one day ago
        Date orderDate1 = new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 48));
        Date delivDate1 = new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24));
        Order preparedOrder = new Order(orderDate1, delivDate1, Order.OrderState.Prepared, customer, employee);
        when(orderRepository.findAll()).thenReturn(List.of(preparedOrder));

        assertDoesNotThrow(() -> orderService.automaticDeliveryUpdate());

        assertEquals(Order.OrderState.Delivered, preparedOrder.getOrderState());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testAutomaticDeliveryUpdateWrongState() {
        // ordered 2 days ago, delivery scheduled to one day ago
        Date orderDate1 = new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 48));
        Date delivDate1 = new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24));
        Order preparedOrder = new Order(orderDate1, delivDate1, Order.OrderState.Ordered, customer, employee);
        when(orderRepository.findAll()).thenReturn(List.of(preparedOrder));

        assertDoesNotThrow(() -> orderService.automaticDeliveryUpdate());

        assertEquals(Order.OrderState.Cancelled, preparedOrder.getOrderState());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    //------------------------
    //Use Loyalty Points test -> private helper function, no testing
    //------------------------


    //------------------------
    //Add Loyalty Points -> private helper function, no testing
    //------------------------


    //------------------------
    //Redeem Loyalty Points test
    //------------------------
    @Test
    public void redeemLoyaltyPointsTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);

        //act
        String msg1 = orderService.redeemLoyaltyPts(fakePerson.getId(), fakeOrder.getId());
        String msg2 = orderService.redeemLoyaltyPts(fakePerson.getId(), fakeOrder.getId());
        fakePerson.setLoyaltyPts(20);
        String msg3 = orderService.redeemLoyaltyPts(fakePerson.getId(), fakeOrder.getId());

        //assertion
        assertEquals("You don't have enough points to get a discount :(", msg1);
        assertEquals("Points not used.", msg2);
        assertEquals("You saved $0.00 on this order!", msg3);
    }


    //------------------------
    //Award Loyalty Points tests
    //------------------------
    @Test
    public void awardLoyaltyPointsTest(){
        //arrange
        when(accountRepository.findById(CUSTOMER_ACCOUNT_ID)).thenReturn(Optional.of(customer));
        Account fakePerson = accountRepository.findById(CUSTOMER_ACCOUNT_ID).orElse(null);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Order fakeOrder = orderRepository.findById(ORDER_ID).orElse(null);
        when(orderRepository.findById(ORDER_ID_DUMMY)).thenReturn(Optional.of(orderDummy));
        Order fakeOrderDummy = orderRepository.findById(ORDER_ID_DUMMY).orElse(null);
        when(orderRepository.findByCustomer_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(List.of(fakeOrder));

        //act
        orderService.awardLoyaltyPts(fakePerson.getId(), fakeOrder.getId());
        assertEquals(0, fakePerson.getLoyaltyPts());
        fakeOrderDummy.setCustomer(fakePerson);
        //just to make sure it actually saved to the db with the proper account
        assertEquals(fakeOrderDummy.getCustomer().getId(), fakePerson.getId());
        when(orderRepository.findByCustomer_Id(CUSTOMER_ACCOUNT_ID)).thenReturn(List.of(fakeOrder, fakeOrderDummy));
        orderService.awardLoyaltyPts(fakePerson.getId(), fakeOrder.getId());
        assertEquals(1, fakePerson.getLoyaltyPts());
    }
}
