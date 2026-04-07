package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.OrderItem;
import ca.mcgill.ecse321.ecse321project03.service.OrderService;
import ca.mcgill.ecse321.ecse321project03.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;

import java.util.List;

/**
 * REST controller responsible for handling all order-related API endpoints.
 * It connects HTTP requests from the client to the OrderService layer.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderRepository orderRepository;


    /**
     * Creates an order and performs checkout in a single operation.
     * This endpoint creates the order, converts the customer's cart items
     * into OrderItems, and returns the full OrderResponseDto containing
     * the order and its items.
     *
     * @param orderCreationDto the DTO containing the customer id and delivery date
     * @return a ResponseEntity containing the created OrderResponseDto if successful,
     *         or an ErrorDto if checkout fails
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto createOrderAndCheckout(@RequestBody OrderCreationDto orderCreationDto) {
            List<OrderItem> orderItems = orderService.checkout(orderCreationDto);
            // Retrieve the created order from the first OrderItem
            Order order = orderItems.get(0).getOrderItemId().getOrder();

            return new OrderResponseDto(order, orderItems);

    }

    /**
     * Retrieves an order by its id.
     *
     * @param id the id of the order to retrieve
     * @return a ResponseEntity containing the OrderResponseDto if the order exists,
     *         or an ErrorDto if the order cannot be found
     */
    @GetMapping("/order/{id}")
    public OrderResponseDto findOrderById(@PathVariable int id) {
            Order order = orderService.findOrderByIdResponseDto(id);
            List<OrderItem> orderItems = orderService.findOrderItems(order);
            return (new OrderResponseDto(order, orderItems));

    }

    /**
     * Assigns an employee to an order.
     *
     * @param orderId the id of the order
     * @param employeeId the id of the employee to assign
     * @param accountId the id of the account making the assignment
     * @return a ResponseEntity containing the updated OrderResponseDto if successful,
     *         or an ErrorDto if the assignment fails
     */
    @PutMapping("/order/{orderId}/assign-employee")
    public OrderResponseDto assignEmployee(@PathVariable int orderId,
            @RequestParam int employeeId, @RequestParam int accountId) {

            Order order = orderService.assignEmployee(orderId, employeeId, accountId);
            List<OrderItem> orderItems = orderService.findOrderItems(order);
            return new OrderResponseDto(order, orderItems);

    }

    /**
     * Updates the status of an order.
     *
     * @param orderId the id of the order to update
     * @param orderState the new order state
     * @param accountId the id of the account making the update
     * @return a ResponseEntity containing the updated OrderResponseDto if successful,
     *         or an ErrorDto if the update fails
     */
    @PutMapping("/order/{orderId}/status")
    public OrderResponseDto updateStatus(@PathVariable int orderId,
            @RequestParam String orderState, @RequestParam int accountId) {
            Order order = orderService.updateStatus(orderId, orderState, accountId);
            List<OrderItem> orderItems = orderService.findOrderItems(order);
            return (new OrderResponseDto(order, orderItems));
    }


    /**
     * Retrieves all orders in the system.
     *
     * @param userID the id of the staff member requesting the orders
     * @return a ResponseEntity containing a list of OrderResponseDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/order")
    public List<OrderResponseDto> viewOrders(@RequestParam int userID) {

            List<Order> orders = orderInfoService.viewOrders(userID);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return orderDtos;
    }

    /**
     * Retrieves all orders sorted by order date.
     *
     * @param userID the id of the staff member requesting the orders
     * @param filterDirection the sorting direction, either ascending or descending
     * @return a ResponseEntity containing a list of OrderResponseDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/order-date")
    public List<OrderResponseDto> viewOrdersByOrderDate(@RequestParam int userID, @RequestParam String filterDirection) {

            List<Order> orders = orderInfoService.viewOrdersByOrderDate(userID, filterDirection);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return orderDtos;

    }

    /**
     * Retrieves all orders sorted by customer.
     *
     * @param userID the id of the staff member requesting the orders
     * @param filterDirection the sorting direction, either ascending or descending
     * @return a ResponseEntity containing a list of OrderResponseDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/order-customer")
    public List<OrderResponseDto> viewOrdersByCustomer(@RequestParam int userID, @RequestParam String filterDirection) {

            List<Order> orders = orderInfoService.viewOrdersByCustomer(userID, filterDirection);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return orderDtos;

    }

    /**
     * Retrieves all orders sorted by status.
     *
     * @param userID the id of the staff member requesting the orders
     * @param filterDirection the sorting direction, either ascending or descending
     * @return a ResponseEntity containing a list of OrderResponseDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/order-status")
    public List<OrderResponseDto> sortOrdersByStatus(@RequestParam int userID, @RequestParam String filterDirection) {
            List<Order> orders = orderInfoService.sortOrdersByStatus(userID, filterDirection);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return orderDtos;
    }

    /**
     * Redeems loyalty points for a specific order belonging to an account.
     *
     *
     * @param orderId the ID of the order where points are redeemed
     * @param accountId the ID of the account using loyalty points
     * @param usePoints the choice of redeeming loyalty points
     * @return a ResponseEntity containing the updated AccountResponseDto if successful,
     *         or an ErrorDto if redemption fails
     */
    @PostMapping("/redeem-loyalty/{orderId}")
    public String redeemLoyaltyPtsInOrder( @PathVariable int orderId,
                                           @RequestParam int accountId, @RequestParam boolean usePoints) {
            String message = orderService.redeemLoyaltyPts(accountId, orderId, usePoints);
            return message;
    }
}
