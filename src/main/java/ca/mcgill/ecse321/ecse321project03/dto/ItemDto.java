package ca.mcgill.ecse321.ecse321project03.dto;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.Item.Tag;
// Reference: Turtorial 7
public class ItemDto {

    // displayed fields
    private int id;
    private String name;
    private Tag tag;
    private double price;

    public ItemDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.tag = item.getTag();
        this.price = item.getPrice();
    }

    @SuppressWarnings("unused")
    public ItemDto() {  
    }

    // Getter methods

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Tag getTag() {
        return tag;
    }

    public double getPrice() {
        return price;
    }

    
}
