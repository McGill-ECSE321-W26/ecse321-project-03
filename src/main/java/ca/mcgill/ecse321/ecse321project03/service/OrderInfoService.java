package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.EmployeeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Service
public class OrderInfoService {
    @Autowired
    private OrderRepository orderRepository;


    /**
     * Get all DTO orders, ready to display
     * @return A list of OrderResponseDto objects
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrders() {
        return getAllOrders();
    }

    /**
     * Filter the DTO orders, in ascending or descending order date
     * @return A list of OrderResponseDto objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByOrderDate() {
        List<OrderResponseDto> allOrdersDTO = getAllOrders();

        allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getOrderDate));

        return allOrdersDTO;
    }

    /**
     * Filter the DTO orders, by order date
     * @return A list of OrderResponseDto objects sorted by order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByOrderDate(String filterDirection) {
        List<OrderResponseDto> allOrdersDTO = getAllOrders();
        if (filterDirection.equals("descending")) {
            allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getOrderDate).reversed());
        } else {
            allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getOrderDate));
        }

        return allOrdersDTO;
    }

    /**
     * Filter the DTO orders, by customer
     * @return A list of OrderResponseDto objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByCustomer(String filterDirection) {
        List<OrderResponseDto> allOrdersDTO = getAllOrders();
        if (filterDirection.equals("descending")) {
            allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getCustomerId).reversed());
        } else {
            allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getCustomerId));
        }

        return allOrdersDTO;
    }

    /**
     * Filter the DTO orders, by status
     * @return A list of OrderResponseDto objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByStatus(String filterDirection) {
        List<OrderResponseDto> allOrdersDTO = getAllOrders();
        if (filterDirection.equals("descending")) {
            allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getOrderState).reversed());
        } else {
            allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getOrderState));
        }

        return allOrdersDTO;
    }

    /**
     * Service method to get all orders in DTO form
     * @return A list of OrderResponseDto objects corresponding to all the orders in the system
     * @author Sonia Ly
     */
    private List<OrderResponseDto> getAllOrders() {
        List<Order> allOrders = orderRepository.findAll();
        List<OrderResponseDto> allOrdersDTO = new ArrayList<>();

        for (Order order: allOrders) {
            OrderResponseDto currentOrder = new OrderResponseDto(order);
            allOrdersDTO.add(currentOrder);
        }

        return allOrdersDTO;
    }


}
