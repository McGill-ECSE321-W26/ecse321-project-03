package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.model.OrderItem;
import ca.mcgill.ecse321.ecse321project03.service.OrderService;
import ca.mcgill.ecse321.ecse321project03.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

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
    public ResponseEntity<?> createOrderAndCheckout(@RequestBody OrderCreationDto orderCreationDto) {
        try {
            List<OrderItem> orderItems = orderService.checkout(orderCreationDto);
            // Retrieve the created order from the first OrderItem
            Order order = orderItems.get(0).getOrderItemId().getOrder();

            return new ResponseEntity<>(new OrderResponseDto(order, orderItems), HttpStatus.CREATED);

        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Retrieves an order by its id.
     *
     * @param id the id of the order to retrieve
     * @return a ResponseEntity containing the OrderResponseDto if the order exists,
     *         or an ErrorDto if the order cannot be found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findOrderById(@PathVariable int id) {
        try {
            Order order = orderService.findOrderByIdResponseDto(id);
            List<OrderItem> orderItems = orderService.findOrderItems(order);
            return new ResponseEntity<>(new OrderResponseDto(order, orderItems), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
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
    @PutMapping("/{orderId}/assign")
    public ResponseEntity<?> assignEmployee(@PathVariable int orderId,
            @RequestParam int employeeId, @RequestParam int accountId) {
        try {
            Order order = orderService.assignEmployee(orderId, employeeId, accountId);
            List<OrderItem> orderItems = orderService.findOrderItems(order);
            return new ResponseEntity<>(new OrderResponseDto(order, orderItems), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
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
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int orderId,
            @RequestParam String orderState, @RequestParam int accountId) {
        try {
            Order order = orderService.updateStatus(orderId, orderState, accountId);
            List<OrderItem> orderItems = orderService.findOrderItems(order);
            return new ResponseEntity<>(new OrderResponseDto(order, orderItems), HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }


    /**
     * Retrieves all orders in the system.
     *
     * @param userID the id of the staff member requesting the orders
     * @return a ResponseEntity containing a list of OrderResponseDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping
    public ResponseEntity<?> viewOrders(@RequestParam int userID) {
        try {
            List<Order> orders = orderInfoService.viewOrders(userID);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return new ResponseEntity<>(orderDtos, HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
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
    public ResponseEntity<?> viewOrdersByOrderDate(@RequestParam int userID, @RequestParam String filterDirection) {
        try {
            List<Order> orders = orderInfoService.viewOrdersByOrderDate(userID, filterDirection);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return new ResponseEntity<>(orderDtos, HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Retrieves all orders sorted by customer.
     *
     * @param userID the id of the staff member requesting the orders
     * @param filterDirection the sorting direction, either ascending or descending
     * @return a ResponseEntity containing a list of OrderResponseDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/customer")
    public ResponseEntity<?> viewOrdersByCustomer(@RequestParam int userID, @RequestParam String filterDirection) {
        try {
            List<Order> orders = orderInfoService.viewOrdersByCustomer(userID, filterDirection);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return new ResponseEntity<>(orderDtos, HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Retrieves all orders sorted by status.
     *
     * @param userID the id of the staff member requesting the orders
     * @param filterDirection the sorting direction, either ascending or descending
     * @return a ResponseEntity containing a list of OrderResponseDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/status")
    public ResponseEntity<?> sortOrdersByStatus(@RequestParam int userID, @RequestParam String filterDirection) {
        try {
            List<Order> orders = orderInfoService.sortOrdersByStatus(userID, filterDirection);
            List<OrderResponseDto> orderDtos = orders.stream()
                    .map(order -> new OrderResponseDto(order, orderService.findOrderItems(order)))
                    .toList();
            return new ResponseEntity<>(orderDtos, HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
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
    @PostMapping("/{id}/redeem-loyalty")
    public ResponseEntity<?> redeemLoyaltyPtsInOrder( @PathVariable int orderId,
                                                      @RequestParam int accountId, @RequestParam boolean usePoints) {
        try {
            String message = orderService.redeemLoyaltyPts(accountId, orderId, usePoints);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }
}
