package ca.mcgill.ecse321.ecse321project03.exception;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

// tutorial 6
public class Ecse321Project03Exception extends RuntimeException {
    private HttpStatus status;

    public Ecse321Project03Exception(@NonNull HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}