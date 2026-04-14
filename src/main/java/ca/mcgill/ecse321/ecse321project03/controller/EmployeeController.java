package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.EmployeeDto;

import ca.mcgill.ecse321.ecse321project03.model.Employee;

import ca.mcgill.ecse321.ecse321project03.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;



    /**
     * Retrieves all employees in the system.
     * @param userID : account id performing the action
     * @return a ResponseEntity containing a list of EmployeeDto objects if successful,
     *         or an ErrorDto if the request fails
     * @author : Wissam Abchiche
     */
    @GetMapping("")
    public List<EmployeeDto> viewEmployees(@RequestParam int userID) {

            List<Employee> employees = employeeService.viewEmployees(userID);
            List<EmployeeDto> employeeDtos = employees.stream()
                    .map(employee -> new EmployeeDto(employee))
                    .toList();
            return employeeDtos;
    }



}
