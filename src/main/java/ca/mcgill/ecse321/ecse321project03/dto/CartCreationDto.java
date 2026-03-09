package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.Account;
import jakarta.validation.constraints.NotBlank;

public class CartCreationDto {

    @NotBlank(message = "The cart must be associated to an account.")
    private Account account;

    @SuppressWarnings("unused")
    public CartCreationDto(){
    }

    public CartCreationDto(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return this.account;
    }

}
