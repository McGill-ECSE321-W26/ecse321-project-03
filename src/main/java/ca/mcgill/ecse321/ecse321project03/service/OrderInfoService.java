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
     * Get all orders, ready to display
     *
     * @return A list of order objects
     * @author Sonia Ly
     */
    @Transactional
    public List<Order> viewOrders(int userID) {
        checkIfStaffMember(userID);
        
        return (List<Order>) orderRepository.findAll();
    }
    

    /**
     * Filter the orders, by order date
     *
     * @return A list of Order objects sorted by order date
     * @author Sonia Ly
     */
    @Transactional
    public List<Order> viewOrdersByOrderDate(int userID, String filterDirection) {
        checkIfStaffMember(userID);

        List<Order> allOrders = (List<Order>) orderRepository.findAll();
        if (filterDirection.equals("descending")) {
            allOrders.sort(Comparator.comparing(Order::getOrderDate).reversed());
        } else {
            allOrders.sort(Comparator.comparing(Order::getOrderDate));
        }

        return allOrders;
    }

    /**
     * Filter the orders, by customer
     *
     * @return A list of Order objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<Order> viewOrdersByCustomer(int userID, String filterDirection) {
        checkIfStaffMember(userID);

        List<Order> allOrders = (List<Order>) orderRepository.findAll();
        if (filterDirection.equals("descending")) {
            allOrders.sort(Comparator.comparing((Order o) -> o.getCustomer().getId()).reversed());
        } else {
            allOrders.sort(Comparator.comparing((Order o) -> o.getCustomer().getId()));
        }

        return allOrders;
    }

    /**
     * Filter the  orders, by status
     *
     * @return A list of Order objects sorted in ascending order date
     * @author Sonia Ly
     */
    @Transactional
    public List<Order> viewOrdersByStatus(int userID, String filterDirection) {
        checkIfStaffMember(userID);

        List<Order> allOrders = (List<Order>) orderRepository.findAll();
        if (filterDirection.equals("descending")) {
            allOrders.sort(Comparator.comparing(Order::getOrderState).reversed());
        } else {
            allOrders.sort(Comparator.comparing(Order::getOrderState));
        }

        return allOrders;
    }

    /**
     * Service method to get all orders in DTO form
     * Should to delete that * should be implemented in controller(Wissam)
     * @return A list of OrderResponseDto objects corresponding to all the orders in the system
     * @author Sonia Ly
     */
    //private List<OrderResponseDto> getAllOrders() {
      //  List<Order> allOrders = (List<Order>) orderRepository.findAll();
       // List<OrderResponseDto> allOrdersDTO = new ArrayList<>();

     //   for (Order order : allOrders) {
         //   OrderResponseDto currentOrder = new OrderResponseDto(order);
        //    allOrdersDTO.add(currentOrder);
       // }//

     //   return allOrdersDTO;
   // }


    /**
     * Helper method to determine if a user is a Staff member from their user ID. Throws an error if it isn't the case.
     * @param userID
     * @return
     */
    private void checkIfStaffMember(int userID) {
        if(roleRepository.findByAccount_Id(userID) == null){
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Customers may not view of all of the orders in the system");
        }
    }
}
