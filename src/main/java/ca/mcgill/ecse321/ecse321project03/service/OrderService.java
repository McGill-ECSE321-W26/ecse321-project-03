package ca.mcgill.ecse321.ecse321project03.service;



import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.EmployeeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.sql.Date;
import java.util.List;

@Service
@Validated
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;


    /**
     * Create an order based on the OrderCreationDTO transfer object (where only the customer id and the chosen delivery date are provided in the UI)
     * @author Wissam Abchiche
     * @param orderDto The transfer object
     * @return the Order object
     */
    @Transactional
    public Order createOrder(@Valid OrderCreationDto orderDto) {

        // Verify that the customer id entered by the customer when making the order is valid.
        // Otherwise, throw an error
        Account customer = accountRepository.findById(orderDto.getCustomerId());
        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No customer found with id " + orderDto.getCustomerId());
        }


        // Get the current date as the order date
        Date orderDate = new Date(System.currentTimeMillis());


        // Verify that the delivery date has been chosen and that it was chosen at least 24 hours after the order date
        if (orderDto.getDelivDate() == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No delivery date found");

        } else if (((orderDto.getDelivDate().getTime() - orderDate.getTime()) / (60 * 60 * 1000)) < 24) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "The delivery date " + orderDto.getDelivDate() +
                    " can not be placed less than 24 hours after the order date " + orderDate);
        }

        // Create the Order object. For now, the employee assigned is null and the state is Ordered.
        Order newOrder = new Order(
                orderDate,
                orderDto.getDelivDate(),
                Order.OrderState.Ordered,
                customer,
                null);


        return orderRepository.save(newOrder);
    }

    /**
     * Find an order corresponding to a given id
     * @author Wissam Abchiche
     * @param id Order id
     * @return The order with the given id
     */
    @Transactional
    public Order findOrderById(int id) {
        Order orderFromDB = orderRepository.findById(id);
        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No order found with id " + id);
        }
        return orderFromDB;
    }

    /**
     * Find an order corresponding to a given id
     * @author Wissam Abchiche
     * @param id Order id
     * @return The OrderResponse Dto object that is used to display the order information in the UI
     */
    @Transactional
    public OrderResponseDto findOrderByIdResponseDto(int id) {
        Order orderFromDB = findOrderById(id);
        return new OrderResponseDto(orderFromDB);
    }

    /**
     * Assign the order to an employee and ensure the employee is not the customer
     * @author Wissam Abchiche
     * @param orderId OrderId to which an employee is assigned
     * @param employeeId The employee id
     * @return The modified order (after assigning an employee)
     */
    @Transactional
    public Order assignEmployee(int orderId, int employeeId) {

        // Verify order exists
        Order order = findOrderById(orderId);

        // Verify that the employee with the given id exists and that the employee is not the one who made the order
        Employee assignee = employeeRepository.findById(employeeId);
        if (assignee == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No employee found with id " + employeeId);
        } else if (assignee.getAccount().getId() == order.getCustomer().getId() ) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "The order cannot be assigned to the employee with id " + employeeId + " as he is the customer.");
        }

        // Assign the employee to the order
        order.setAssignee(assignee);

        return orderRepository.save(order);
    }

    /**
     * Verify that the order status is at a certain state before changing its status to the new state.
     * If everything is respected, change the state of the order
     * @param orderFromDB Order for which the state must be changed
     * @param orderStateOld State that the order must be in to change its state to a new one
     * @param orderStateNew New state the order must change to
     */
    private void verifyUpdateStatus(Order orderFromDB, Order.OrderState orderStateOld, Order.OrderState orderStateNew) {

        // Check that the state of the order corresponds to what it must be given a new state. Otherwise, throw an error
        if (orderFromDB.getOrderState() !=  orderStateOld) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Order with id " + orderFromDB.getId() + " cannot be switched to " + orderStateNew.toString() + " because it is not " + orderStateOld);
        }

        // Change the state
        orderFromDB.setOrderState(orderStateNew);
    }


    /**
     * Verify that the cancellation of an order made by an account is valid (i.e. either a customer or a manager has made the cancellation)
     * Also, verify that, if a customer made a cancellation, it is more than 24 hours from the delivery date
     * @author Wissam Abchiche
     * @param order The order to cancel
     * @param accountId The accountId who made the cancellation
     */
    private void cancelOrder(Order order, int accountId) {


        // Verify that the account who is cancelling the order exists
        Account account = accountRepository.findById(accountId);
        if (account == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with id " + accountId);
        }

        // Verify if account is a role (i.e. an employee or a manager
        Role role = roleRepository.findByAccount_Id(accountId);

        if (role == null) { // If role is null, that means a customer made a cancellation

            // Verify that the cancellation is made more than 24 hours from the delivery date. Otherwise, throw an error
            Date currentDate = new Date(System.currentTimeMillis());
            if (((order.getDelivDate().getTime() - currentDate.getTime()) / (60 * 60 * 1000)) < 24) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "The order cannot be cancelled less " +
                        "than 24 hours before the delivery date " + order.getDelivDate());
            }

            // Set the state of order to Cancelled if no error was found
            order.setOrderState(Order.OrderState.Cancelled);

        } else if (role instanceof Employee) { // If account is an employee, throw an error
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "The order cannot be cancelled by an employee");

        } else  if (role instanceof Manager){ // If account is a manager, set the state of the order to Cancelled
            order.setOrderState(Order.OrderState.Cancelled);
        }
    }


    /**
     * Update the status of an order to a given status and verify if the change is valid
     * @author Wissam Abchiche
     * @param orderId Order for which the state must change
     * @param orderStateNew New state of the order
     * @param accountId Account who is making the change
     * @return the updated order
     */
    @Transactional
    public Order updateStatus(int orderId, Order.OrderState orderStateNew, int accountId) {

        // Verify order exists
        Order orderFromDB = findOrderById(orderId);

        // Update status
         if (orderStateNew ==  Order.OrderState.Cancelled) {
            cancelOrder(orderFromDB, accountId);

            // Verify that the one making the state change is either a Manager or an Employee
        } else if (roleRepository.findByAccount_Id(accountId) != null){
             if (orderStateNew == Order.OrderState.InPreparation) {
                 verifyUpdateStatus(orderFromDB, Order.OrderState.Ordered, orderStateNew);
             } else if (orderStateNew ==  Order.OrderState.Prepared) {
                 verifyUpdateStatus(orderFromDB, Order.OrderState.InPreparation, orderStateNew);
             } else if (orderStateNew ==  Order.OrderState.Delivered) {
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
     * @author Wissam Abchiche
     * @param orderFromDB order for which to update the state
     */
    private void prepareOrderToDeliveryAutomatically(Order orderFromDB) {

        // Verify that order is either in the Prepared or Delivered state when delivered date is reached.
        // Otherwise, cancel the order and throw an error
        if (orderFromDB.getOrderState() !=  Order.OrderState.Prepared || orderFromDB.getOrderState() !=  Order.OrderState.Delivered) {
            orderFromDB.setOrderState(Order.OrderState.Cancelled);
            orderRepository.save(orderFromDB);
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Order with id " + orderFromDB.getId() + " cannot be switched to Delivered automatically because it is not Prepared.");

       // If the order is Prepared by the delivered date, switch its state to Delivered
        } else if (orderFromDB.getOrderState() == Order.OrderState.Prepared) {
            orderFromDB.setOrderState(Order.OrderState.Delivered);
            orderRepository.save(orderFromDB);
        }
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
        List<Order> allOrders = orderRepository.findAll();

        if (allOrders != null) {

            for (Order order : allOrders) {

                // if the current time is equal or exceeds the delivery date, change its state to Delivered
                if (order.getDelivDate().getTime() <= currentTime.getTime())  {
                    prepareOrderToDeliveryAutomatically(order);
                }
            }
        }
    }
}