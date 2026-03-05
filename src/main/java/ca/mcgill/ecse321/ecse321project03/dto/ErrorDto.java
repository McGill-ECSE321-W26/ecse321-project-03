package ca.mcgill.ecse321.ecse321project03.dto;

import java.util.List;

public class ErrorDto { // tutorial 6
    private List<String> errors;

    // Needed by Jackson to deserialize JSON
    @SuppressWarnings("unused")
    public ErrorDto() {
    }

    public ErrorDto(List<String> errors) {
        this.errors = errors;
    }

    public ErrorDto(String error) {
        this.errors = List.of(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
