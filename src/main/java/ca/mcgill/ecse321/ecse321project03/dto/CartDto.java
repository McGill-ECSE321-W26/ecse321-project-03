package ca.mcgill.ecse321.ecse321project03.dto;
import ca.mcgill.ecse321.ecse321project03.model.Cart;


//Reference: Tutorial 7
public class CartDto {
    
    // displayed fields
    private int id;
    private AccountResponseDto account;


    //------------------------
    // CONSTRUCTOR
    //------------------------
    public CartDto(Cart cart) {
        this.id = cart.getId();
        this.account = new AccountResponseDto(cart.getAccount());
    }

    @SuppressWarnings("unused")
    public CartDto() {

    }

    //------------------------
    // INTERFACE
    //------------------------


    // Getter methods
    public int getId() {
        return id;
    }

    public AccountResponseDto getAccount() {
        return account;
    }


    
}
