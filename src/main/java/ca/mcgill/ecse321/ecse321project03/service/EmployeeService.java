package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.Order;
import ca.mcgill.ecse321.ecse321project03.repository.EmployeeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;
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
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    /**
     * Get all employees, ready to display
     *
     * @return A list of employee objects
     * @author Wissam Abchiche
     */
    @Transactional
    public List<Employee> viewEmployees(int userID) {
        checkIfManager(userID);

        return (List<Employee>) employeeRepository.findAll();
    }
    


    /**
     * Helper method to determine if a user is a Manager member from their user ID. Throws an error if it isn't the case.
     * @param userID id of the account
     */
    private void checkIfManager(int userID) {
        if(managerRepository.findByAccount_Id(userID) == null) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Only manager may view all employees in the system");
        }
    }
}
