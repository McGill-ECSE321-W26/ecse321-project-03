package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.AccountResponseDto;
import ca.mcgill.ecse321.ecse321project03.dto.EmployeeDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.service.AccountService;
import ca.mcgill.ecse321.ecse321project03.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


public class EmployeeController {

     @Autowired
    private EmployeeService employeeService;



    /**
     * Retrieves all employees in the system.
     * @param userID : account id performing the action
     * @return a ResponseEntity containing a list of EmployeeDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/employee")
    public List<EmployeeDto> viewEmployees(@RequestParam int userID) {

            List<Employee> employees = employeeService.viewEmployees(userID);
            List<EmployeeDto> employeeDtos = employees.stream()
                    .map(employee -> new EmployeeDto(employee))
                    .toList();
            return employeeDtos;
    }

    
    
}
