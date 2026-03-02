package ca.mcgill.ecse321.ecse321project03.exception;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.util.ArrayList;
import java.util.List;


// tutorial 6
@ControllerAdvice
public class Ecse321Project03ExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errors = new ArrayList<>();
        for (var error : e.getConstraintViolations()) {
            errors.add(error.getMessage());
        }
        return new ResponseEntity<>(new ErrorDto(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Ecse321Project03Exception.class)
    public ResponseEntity<ErrorDto> handleException(Ecse321Project03Exception e) {
        return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
    }
}