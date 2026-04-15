package ca.mcgill.ecse321.ecse321project03.service;


import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
@Validated
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemImageRepository itemImageRepository;


    /**
     * Create an order based on the OrderCreationDTO transfer object (where only the customer id and the chosen delivery date are provided in the UI)
     *
     * @param orderDto The transfer object created
     * @return the order
     * @author Wissam Abchiche
     */
    @Transactional
    public Order createOrder(@Valid OrderCreationDto orderDto) {

        // This function is called in checkout, which already verifies if account exist. So no need to verify here
        Account customer = accountRepository.findById(orderDto.getCustomerId()).orElse(null);


        // Get the current date as the order date
        Date orderDate = new Date(System.currentTimeMillis());


        // Verify that the delivery date has been chosen at least 24 hours after the order date
         if (((orderDto.getDelivDate().getTime() - orderDate.getTime()) / (60L * 60L * 1000L)) < 24) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "The delivery date can not be placed less than 24 hours after the order date");
        }

         // Verify that the delivery date has been chosen at least 24 hours after the order date
         if (orderDto.getAddress() == null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "The address cannot be null.");
        }

        // Create the Order object. For now, the employee assigned is null and the state is Ordered.
        Order newOrder = new Order(
                orderDate,
                orderDto.getDelivDate(),
                Order.OrderState.Ordered,
                customer,
                null, orderDto.getAddress());
        Order savedOrder = orderRepository.save(newOrder);
        return savedOrder;
    }


    /**
     * Perform the checkout process for a customer's cart.
     *
     * This method retrieves the customer's current cart, validates that the cart
     * contains items and that each item has sufficient stock available, then
     * converts each CartItem into an OrderItem linked to the given Order.
     * The stock of each corresponding ItemType is decreased accordingly.
     * After the OrderItems are created successfully, the customer's cart is cleared.
     *
     * This method is intended to be called after an Order entity has been created.
     * This corresponds to the "Checkout" use case.
     *
     * @author Yani Zhang
     * @param orderDto contains the customer id and chosen delivery date
     * @return a list of OrderItems created from the customer's cart
     */
    @Transactional
    public List<OrderItem> checkout(@Valid OrderCreationDto orderDto) {

        // 1) Find the customer's cart
        Cart cart = cartRepository.findByAccount_Id(orderDto.getCustomerId());
        if (cart == null) { // meaning the cart doesn't exist or the customer id isn't valid
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No cart found for customer with id " + orderDto.getCustomerId());
        }

        // 2) Load all cart items
        List<CartItem> cartItems = cartItemRepository.findByCartItemId_Cart(cart);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Cannot checkout with an empty cart");
        }

        // 3) Validate stock first
        for (CartItem cartItem : cartItems) {
            int qty = cartItem.getQuantity();

            if (qty <= 0) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                        "Cart contains an invalid quantity: " + qty);
            }

            ItemType itemType = cartItem.getCartItemId().getItemType();

            if (itemType == null) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                        "Cart contains an item with no item type");
            }

            if (itemType.getStock() < qty) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                        "Not enough stock for itemType id " + itemType.getId() +
                                ". Requested " + qty + ", available " + itemType.getStock());
            }
        }

        // 4) Create OrderItems + decrement stock
        Order order = createOrder(orderDto); // saves order
        Account customer = order.getCustomer();

        List<OrderItem> createdOrderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            int qty = cartItem.getQuantity();
            ItemType itemType = cartItem.getCartItemId().getItemType();

            OrderItem.OrderItemId orderItemId = new OrderItem.OrderItemId(itemType, order);
            OrderItem orderItem = new OrderItem(orderItemId, qty);
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            createdOrderItems.add(savedOrderItem);

            itemType.setStock(itemType.getStock() - qty);
            itemTypeRepository.save(itemType);
        }

        double finalPriceAfterDiscount = calculateFinalPriceAfterDiscount(customer.getId(), order.getId());
        order.setTotalPriceAfterDiscount(finalPriceAfterDiscount);
        orderRepository.save(order);

        // 5) Clear cart
        cartItemRepository.deleteAll(cartItems);

        awardLoyaltyPts(customer.getId(), order.getId());
        return createdOrderItems;
    }


    /**
     * Find an order corresponding to a given id
     *
     * @param id Order id
     * @return The found order object
     * @author Wissam Abchiche
     */
    @Transactional
    public Order findOrderByIdResponseDto(int id) {
        Order orderFromDB = orderRepository.findById(id).orElse(null);
        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No order found with id " + id);
        }
        return orderFromDB;
    }



    /**
     * Assign the order to an employee, ensure the employee is not the customer
     * Ensure it's not a customer who is making the change and ensure an employee is not assigning an order to another employee except himself
     *
     * @param orderId    OrderId to which an employee is assigned
     * @param employeeId The employee id
     * @param accountId  The person assigning an employee to the order
     * @return The modified order object (after assigning an employee)
     * @author Wissam Abchiche
     */
    @Transactional
    public Order assignEmployee(int orderId, int employeeId, int accountId) {


        // Verify order exists
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No order found with id " + orderId);
        }

        Account account = accountRepository.findById(accountId).orElse(null);

        // Verify account making the change exists
        if (account == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account doing the action found with id " + accountId);
            // If the account is not associated to a role, it means a customer is making the change. Throw an error
        } else if (roleRepository.findByAccount_Id(accountId) == null) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "The order cannot be assigned to the employee with id " + employeeId + " as a customer is making the change.");
        }


        // Verify that the employee with the given id exists and that the employee is not the one who made the order
        Employee assignee = employeeRepository.findById(employeeId).orElse(null);
        if (assignee == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No employee found with id " + employeeId);
        } else if (assignee.getAccount().getId() == order.getCustomer().getId()) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "The order cannot be assigned to the employee with id " + employeeId + " as he is the customer.");
        }

        // Verify that, if the account trying to make the change is an employee, he is not assigning the order to another employee than himself
        Role role = roleRepository.findByAccount_Id(accountId);
        if (role instanceof Employee) {
            if (roleRepository.findByAccount_Id(assignee.getAccount().getId()).getId() != role.getId()) {
                throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "The order cannot be assigned to the employee by another employee.");
            }
        }

        // Assign the employee to the order
        order.setAssignee(assignee);

        return orderRepository.save(order);
    }


    /**
     * Verify that the order status is at a certain state before changing its status to the new state.
     * If everything is respected, change the state of the order
     *
     * @param orderFromDB   Order for which the state must be changed
     * @param orderStateOld State that the order must be in to change its state to a new one
     * @param orderStateNew New state the order must change to
     */
    private void verifyUpdateStatus(Order orderFromDB, Order.OrderState orderStateOld, Order.OrderState orderStateNew) {

        // Check that the state of the order corresponds to what it must be given a new state. Otherwise, throw an error
        if (orderFromDB.getOrderState() != orderStateOld) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Order with id " + orderFromDB.getId() + " cannot be switched to " + orderStateNew.toString() + " because it is not " + orderStateOld);
        }

        // Change the state
        orderFromDB.setOrderState(orderStateNew);
    }


    /**
     * Verify that the cancellation of an order made by an account is valid (i.e., either a customer or a manager has made the cancellation).
     * Also verify that, if a customer made a cancellation, it is more than 24 hours from the delivery date.
     * If the cancellation is valid, mark the order as Cancelled and restock the corresponding ItemType stock based on the order's OrderItems.
     *
     * @author Wissam Abchiche
     * @author Yani Zhang
     *
     * @param order The order to cancel
     * Verify that the cancellation of an order made by an account is valid (i.e. either a customer or a manager has made the cancellation)
     * Also, verify that, if a customer made a cancellation, it is more than 24 hours from the delivery date
     *
     * @param order     The order to cancel
     * @param accountId The accountId who made the cancellation
     * @author Wissam Abchiche
     */
    private void cancelOrder(Order order, int accountId) {


        // Verify that the account who is cancelling the order exists
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with id " + accountId);
        }

        // Verify if account is a role (i.e. an employee or a manager)
        Role role = roleRepository.findByAccount_Id(accountId);

        if (role == null) { // If role is null, that means a customer made a cancellation

            // Verify that the cancellation is made more than 24 hours from the delivery date. Otherwise, throw an error
            Date currentDate = new Date(System.currentTimeMillis());
            if (((order.getDelivDate().getTime() - currentDate.getTime()) / (60L * 60L * 1000L)) < 24) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "The order cannot be cancelled less " +
                        "than 24 hours before the delivery date");
            }

            // Set the state of order to Cancelled if no error was found
            order.setOrderState(Order.OrderState.Cancelled);

        } else if (role instanceof Employee) { // If account is an employee, throw an error
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "The order cannot be cancelled by an employee");

        } else if (role instanceof Manager) { // If account is a manager, set the state of the order to Cancelled
            order.setOrderState(Order.OrderState.Cancelled);
        }

        // ------------------------
        // Restock inventory on successful cancellation
        // ------------------------
        List<OrderItem> orderItems = orderItemRepository.findByOrderItemId_Order(order);
        if (orderItems != null) {
            for (OrderItem oi : orderItems) {
                int qty = oi.getQuantity();
                if (qty <= 0) {
                    continue;
                }
                ItemType itemType = oi.getOrderItemId().getItemType();
                if (itemType == null) {
                    continue;
                }
                itemType.setStock(itemType.getStock() + qty);
                itemTypeRepository.save(itemType);
            }
        }
    }


    /**
     * Update the status of an order to a given status and verify if the change is valid
     *
     * @param orderId    Order for which the state must change
     * @param orderState New state of the order
     * @param accountId  Account who is making the change
     * @return the updated Order object
     * @author Wissam Abchiche
     */
    @Transactional
    public Order updateStatus(int orderId, String orderState, int accountId) {

        // Verify order exists
        Order orderFromDB = orderRepository.findById(orderId).orElse(null);
        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No order found with id " + orderId);
        }

        Order.OrderState orderStateNew = Order.OrderState.valueOf(orderState);
        // Update status
        if (orderStateNew == Order.OrderState.Cancelled) {
            cancelOrder(orderFromDB, accountId);

            // Verify that the one making the state change is either a Manager or an Employee
        } else if (roleRepository.findByAccount_Id(accountId) != null) {
            if (orderStateNew == Order.OrderState.InPreparation) {
                verifyUpdateStatus(orderFromDB, Order.OrderState.Ordered, orderStateNew);
            } else if (orderStateNew == Order.OrderState.Prepared) {
                verifyUpdateStatus(orderFromDB, Order.OrderState.InPreparation, orderStateNew);
            } else if (orderStateNew == Order.OrderState.Delivered) {
                verifyUpdateStatus(orderFromDB, Order.OrderState.Prepared, orderStateNew);
            }

            // if the account is not a role, it means the customer is trying to update the state of the order
            // So throw an error
        } else if (roleRepository.findByAccount_Id(accountId) == null) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "The order state cannot be updated by a customer");
        }

        return orderRepository.save(orderFromDB);
    }


    /**
     * Change the state of an order automatically to Delivered when its delivery date is reached
     *
     * @param orderFromDB order for which to update the state
     * @author Wissam Abchiche
     */
    private void prepareOrderToDeliveryAutomatically(Order orderFromDB) {

        // Verify that order is either in the Prepared or Delivered state when delivered date is reached.
        // Otherwise, cancel the order and throw an error
        if (orderFromDB.getOrderState() != Order.OrderState.Prepared && orderFromDB.getOrderState() != Order.OrderState.Delivered) {
            orderFromDB.setOrderState(Order.OrderState.Cancelled);
            //throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Order with id " + orderFromDB.getId() + " cannot be switched to Delivered automatically because it is not Prepared.");

            // If the order is Prepared by the delivered date, switch its state to Delivered
        } else if (orderFromDB.getOrderState() == Order.OrderState.Prepared) {
            orderFromDB.setOrderState(Order.OrderState.Delivered);
        }
        orderRepository.save(orderFromDB);
    }


    /**
     * @author Wissam Abchiche
     * Verify every one minute the orders of the system and if their delivery date has been attained
     */
    @Scheduled(fixedRate = 60000) // every 1 minute
    @Transactional
    public void automaticDeliveryUpdate() {

        // Get current time
        Date currentTime = new Date(System.currentTimeMillis());

        // Get all orders
        Iterable<Order> allOrders = orderRepository.findAll();


        for (Order order : allOrders) {

            // if the current time is equal or exceeds the delivery date, change its state to Delivered
            if (order.getDelivDate().before(currentTime)) {
                prepareOrderToDeliveryAutomatically(order);
            }
        }
        
    }



    //------------------------
    // Related to Loyalty Points
    //------------------------

    /**
     * Awards Loyalty points
     * @param accountId id of the user receiving the loyalty points
     * @param orderId the ID of the order that triggers the loyalty-point award
     * @author Emily Sun
     */
    @Transactional
    public void awardLoyaltyPts(int accountId, int orderId) {
        Account accountFromDB = accountRepository.findById(accountId).orElse(null);
        Order orderFromDB = orderRepository.findById(orderId).orElse(null);

        if (accountFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No customer found with id " + accountId);
        }

        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No order found with id " + orderId);
        }

        if (orderFromDB.getCustomer() == null || orderFromDB.getCustomer().getId() != accountId) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN,
                    "This order does not belong to the account with id " + accountId + ".");
        }

        List<Order> orderList = orderRepository.findByCustomer_Id(accountId);
        if(orderList.size() > 1){
            addLoyaltyPts(accountFromDB);
        }
        accountRepository.save(accountFromDB);
    }

    /**
     * Adds one loyalty point to the given account.
     *
     * @param account the account whose loyalty points will be increased
     * @author Emily Sun
     */
    private void addLoyaltyPts(Account account){
        int pts = account.getLoyaltyPts();
        pts += 1;
        account.setLoyaltyPts(pts);
    }

    /**
     * Returns a message describing the discount saved on an order based on the account's loyalty level.
     * Loyalty points are not consumed in this calculation.
     *
     * @param accountId id of the user
     * @param orderId id of the order for which the discount message is requested
     * @return a message describing how much money was saved
     * @author Emily Sun and Sonia Ly
     */
    @Transactional
    public String redeemLoyaltyPts (int accountId, int orderId){
        Account accountFromDB = accountRepository.findById(accountId).orElse(null);
        Order orderFromDB = orderRepository.findById(orderId).orElse(null);

        if (accountFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No person with id " + accountId + " found.");
        }
        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No order with id " + orderId + " found.");
        }
        if (orderFromDB.getCustomer() == null || orderFromDB.getCustomer().getId() != accountId) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN,
                    "This order does not belong to the account with id " + accountId + ".");
        }

        double price = findOrderPrice(orderFromDB);
        double discountRate = useLoyaltyPoints(accountFromDB);
        double moneySaved = discountRate * price;
        if (discountRate == 0.0) {
            return "You don't have enough points to get a discount :(";
        }
        return "You saved $" + String.format("%.2f", moneySaved) + " on this order!";

    }

    /**
     * Use loyalty points
     * @param account account that's using the loyalty points
     * @author Emily Sun
     */
    private double useLoyaltyPoints(Account account){
        int lytPts = account.getLoyaltyPts();
        if (lytPts < 5) {
            // 0-4 pts -> 0% off
            return 0.0;
        } else if (lytPts < 10) {
            // 5-9 pts -> 1% off
            return 0.01;
        } else if (lytPts < 15) {
            // 10-14 pts -> 2% off
            return 0.02;
        } else if (lytPts < 20) {
            // 15-19 pts -> 5% off
            return 0.05;
        } else if (lytPts < 35) {
            // 20-34 pts -> 10% off
            return 0.1;
        } else {
            // 35+ pts -> 15% off
            return 0.15;
        }
    }


    /**
     * Find the total price of an order
     * @param orderFromDB the order for which we want to find the price
     * @author Emily Sun
     */
    public double findOrderPrice(Order orderFromDB){
        double price = 0.00;
        List<OrderItem> orderItemList = orderItemRepository.findByOrderItemId_Order(orderFromDB);

        for (OrderItem orderItem : orderItemList){
            ItemType itemType = orderItem.getOrderItemId().getItemType();
            Item item = itemType.getGeneralItem();
            double itemPrice = item.getPrice();
            int quantity = orderItem.getQuantity();
            price += itemPrice * quantity;
        }

        return price;
    }

    /**
     * Calculates the final order price after applying the loyalty discount.
     * Loyalty points are not consumed by this calculation.
     *
     * @param accountId id of the user
     * @param orderId id of the order
     * @return final price after discount
     */
    @Transactional
    public double calculateFinalPriceAfterDiscount(int accountId, int orderId) {
        Account accountFromDB = accountRepository.findById(accountId).orElse(null);
        Order orderFromDB = orderRepository.findById(orderId).orElse(null);

        if (accountFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No person with id " + accountId + " found.");
        }
        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No order with id " + orderId + " found.");
        }
        if (orderFromDB.getCustomer() == null || orderFromDB.getCustomer().getId() != accountId) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN,
                    "This order does not belong to the account with id " + accountId + ".");
        }

        double price = findOrderPrice(orderFromDB);
        double discountRate = useLoyaltyPoints(accountFromDB);
        double finalPriceAfterDiscount = price - (discountRate * price);

        return Math.max(finalPriceAfterDiscount, 0.0);
    }

    /**
     * Retrieves all OrderItems associated with a given Order.
     * This is used by controllers when constructing OrderResponseDto.
     *
     * @param order the order whose items should be retrieved
     * @return list of OrderItem objects belonging to the order
     */
    public List<OrderItem> findOrderItems(Order order) {
        return orderItemRepository.findByOrderItemId_Order(order);
    }


}