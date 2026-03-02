package ca.mcgill.ecse321.ecse321project03.service;



import ca.mcgill.ecse321.ecse321project03.dto.OrderCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.EmployeeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.model.Order;
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

    @Transactional
    public Order createOrder(@Valid OrderCreationDto orderDto) {

        Account customer = accountRepository.findById(orderDto.getCustomerId());

        if (customer == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No customer found with id " + orderDto.getCustomerId());
        }


        Date orderDate = new Date(System.currentTimeMillis());

        if (orderDto.getDelivDate() == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No delivery date found");

        } else if (((orderDto.getDelivDate().getTime() - orderDate.getTime()) / (60 * 60 * 1000)) < 24) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "The delivery date " + orderDto.getDelivDate() +
                    " can not be placed less than 24 hours after the order date " + orderDate);
        }

        Order newOrder = new Order(
                orderDate,
                orderDto.getDelivDate(),
                Order.OrderState.Ordered,
                customer,
                null);
        return orderRepository.save(newOrder);
    }

    @Transactional
    public Order findOrderById(int id) {
        Order orderFromDB = orderRepository.findById(id);
        if (orderFromDB == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No order found with id " + id);
        }
        return orderFromDB;
    }

    @Transactional
    public OrderResponseDto findOrderByIdResponseDto(int id) {
        Order orderFromDB = findOrderById(id);
        return new OrderResponseDto(orderFromDB);
    }

    @Transactional
    public Order assignEmployee(Order order, int employeeId) {
        Employee assignee = employeeRepository.findById(employeeId);

        if (assignee == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No employee found with id " + employeeId);
        }

        order.setAssignee(assignee);
        return orderRepository.save(order);
    }
    private void verifyUpdateStatus(Order orderFromDB, Order.OrderState orderStateOld, Order.OrderState orderStateNew) {
        if (orderFromDB.getOrderState() !=  orderStateOld) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Order with id " + orderFromDB.getId() + " cannot be switched to " + orderStateNew.toString() + " because it is not " + orderStateOld);
        }

        orderFromDB.setOrderState(orderStateNew);
    }

    private void cancelOrder(Order order) {
        Date currentDate = new Date(System.currentTimeMillis());
        if (((order.getDelivDate().getTime() - currentDate.getTime()) / (60 * 60 * 1000)) < 24) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "The order cannot be cancelled less " +
                    "than 24 hours before the delivery date " + order.getDelivDate());
        }
        order.setOrderState(Order.OrderState.Cancelled);
    }


    @Transactional
    public Order updateStatus(int id, Order.OrderState orderStateNew) {
        Order orderFromDB = findOrderById(id);

        if (orderStateNew == Order.OrderState.InPreparation) {
            verifyUpdateStatus(orderFromDB, Order.OrderState.Ordered, orderStateNew);
        } else if (orderStateNew ==  Order.OrderState.Prepared) {
            verifyUpdateStatus(orderFromDB, Order.OrderState.InPreparation, orderStateNew);
        } else if (orderStateNew ==  Order.OrderState.Delivered) {
            verifyUpdateStatus(orderFromDB, Order.OrderState.Prepared, orderStateNew);
        } else if (orderStateNew ==  Order.OrderState.Cancelled) {
            cancelOrder(orderFromDB);
        }
        return orderRepository.save(orderFromDB);
    }


    private void prepareOrderToDeliveryAutomatically(Order orderFromDB) {
        if (orderFromDB.getOrderState() !=  Order.OrderState.Prepared || orderFromDB.getOrderState() !=  Order.OrderState.Delivered) {
            orderFromDB.setOrderState(Order.OrderState.Cancelled);
            orderRepository.save(orderFromDB);
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Order with id " + orderFromDB.getId() + " cannot be switched to Delivered automatically because it is not Prepared.");
        } else if (orderFromDB.getOrderState() == Order.OrderState.Prepared) {
            orderFromDB.setOrderState(Order.OrderState.Delivered);
            orderRepository.save(orderFromDB);
        }
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    @Transactional
    public void automaticDeliveryUpdate() {

        Date currentTime = new Date(System.currentTimeMillis());

        List<Order> allOrders = orderRepository.findAll();

        if (allOrders != null) {

            for (Order order : allOrders) {
                if (order.getDelivDate().getTime() <= currentTime.getTime())  {
                    prepareOrderToDeliveryAutomatically(order);
                }
            }
        }
    }
}