package ca.mcgill.ecse321.ecse321project03.exception;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;


// Reference: Tutorial 6
public class OnlineFashionBoutiqueException extends RuntimeException {
    private HttpStatus status;

    public OnlineFashionBoutiqueException(@NonNull HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
