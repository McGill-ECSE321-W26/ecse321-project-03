package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemImageDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderItemDto;
import ca.mcgill.ecse321.ecse321project03.model.ItemImage;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.OrderItem;
import ca.mcgill.ecse321.ecse321project03.service.CartService;
import ca.mcgill.ecse321.ecse321project03.service.OrderService;
import ca.mcgill.ecse321.ecse321project03.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;

import java.util.List;
import java.util.ArrayList;

/**
 * REST controller responsible for handling all order-related API endpoints.
 * It connects HTTP requests from the client to the OrderService layer.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private CartService cartService;

    /**
     * Creates a list of OrderItemDtos giving a list of OrderItems.
     * Each DTO includes the associated order ID and the first image (if available)
     * @param orderItems list of items to transform into dtos
     * @param orderId the ID of the order associated with the items
     * @return a list of OrderItemDto objects representing the given order items
     */
    private List<OrderItemDto> toOrderItemDtos(List<OrderItem> orderItems, int orderId) {
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            // Find general item associated with that order item
            int generalItemId = orderItem.getOrderItemId().getItemType().getGeneralItem().getId();
            // Find first image associated with that generalItem and create dto
            ItemImage firstImage = cartService.getFirstImageForCartOrOrderItem(generalItemId);
            ItemImageDto imageDto = firstImage == null ? null : new ItemImageDto(firstImage);

            // create order item dto
            orderItemDtos.add(new OrderItemDto(orderItem, orderId, imageDto));
        }

        return orderItemDtos;
    }


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

            List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
            return new OrderResponseDto(order, orderItemDtos);
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
            List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
            return (new OrderResponseDto(order, orderItemDtos));
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
            List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
            return new OrderResponseDto(order, orderItemDtos);
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
            List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
            return (new OrderResponseDto(order, orderItemDtos));
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
                    .map(order -> {
                        List<OrderItem> orderItems = orderService.findOrderItems(order);
                        List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
                        return new OrderResponseDto(order, orderItemDtos);
                    })
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
                    .map(order -> {
                        List<OrderItem> orderItems = orderService.findOrderItems(order);
                        List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
                        return new OrderResponseDto(order, orderItemDtos);
                    })
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
                    .map(order -> {
                        List<OrderItem> orderItems = orderService.findOrderItems(order);
                        List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
                        return new OrderResponseDto(order, orderItemDtos);
                    })
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
                    .map(order -> {
                        List<OrderItem> orderItems = orderService.findOrderItems(order);
                        List<OrderItemDto> orderItemDtos = toOrderItemDtos(orderItems, order.getId());
                        return new OrderResponseDto(order, orderItemDtos);
                    })
                    .toList();
            return orderDtos;
    }

    /**
     * Calculates the final total price of an order after applying the loyalty discount.
     *
     * @param orderId the id of the order
     * @param accountId the id of the account requesting the discounted total
     * @return the final price after discount
     */
    @GetMapping("/{orderId}/final-price")
    public double calculateFinalPriceAfterDiscount(@PathVariable int orderId, @RequestParam int accountId) {
        return orderService.calculateFinalPriceAfterDiscount(accountId, orderId);
    }

    /**
     * Redeems loyalty points for a specific order belonging to an account.
     *
     *
     * @param orderId the ID of the order where points are redeemed
     * @param accountId the ID of the account using loyalty points
     * @return a ResponseEntity containing the updated AccountResponseDto if successful,
     *         or an ErrorDto if redemption fails
     */
    @PostMapping("/redeem-loyalty/{orderId}")
    public String redeemLoyaltyPtsInOrder( @PathVariable int orderId,
                                           @RequestParam int accountId) {
        String message = orderService.redeemLoyaltyPts(accountId, orderId);
        return message;
    }

}
