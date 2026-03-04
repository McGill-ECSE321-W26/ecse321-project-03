package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.dto.OrderResponseDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.repository.OrderRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class OrderInfoService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Get all DTO orders, ready to display
     *
     * @return A list of OrderResponseDto objects
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrders(int userID) {
        checkIfStaffMember(userID);
        
        return getAllOrders();
    }

    /**
     * Filter the DTO orders, in ascending or descending order date
     *
     * @return A list of OrderResponseDto objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByOrderDate(int userID) {
        checkIfStaffMember(userID);

        List<OrderResponseDto> allOrdersDTO = getAllOrders();
        allOrdersDTO.sort(Comparator.comparing(OrderResponseDto::getOrderDate));

        return allOrdersDTO;
    }

    /**
     * Filter the DTO orders, by order date
     *
     * @return A list of OrderResponseDto objects sorted by order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByOrderDate(int userID, String filterDirection) {
        checkIfStaffMember(userID);

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
     *
     * @return A list of OrderResponseDto objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByCustomer(int userID, String filterDirection) {
        checkIfStaffMember(userID);

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
     *
     * @return A list of OrderResponseDto objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<OrderResponseDto> viewOrdersByStatus(int userID, String filterDirection) {
        checkIfStaffMember(userID);

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
     *
     * @return A list of OrderResponseDto objects corresponding to all the orders in the system
     * @author Sonia Ly
     */
    private List<OrderResponseDto> getAllOrders() {
        List<Order> allOrders = orderRepository.findAll();
        List<OrderResponseDto> allOrdersDTO = new ArrayList<>();

        for (Order order : allOrders) {
            OrderResponseDto currentOrder = new OrderResponseDto(order);
            allOrdersDTO.add(currentOrder);
        }

        return allOrdersDTO;
    }

    /**
     * Helper method to determine if a user is a Staff member from their user ID. Throws an error if it isn't the case.
     * @param userID
     * @return
     */
    private void checkIfStaffMember(int userID) {
        if(roleRepository.findByAccount_Id(userID) != null){
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");
        }
    }


}
