package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.ItemType.Color;
import ca.mcgill.ecse321.ecse321project03.model.ItemType.Size;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;   

public class ItemTypeDto {

    private Size size;
    private int id;
    private Color color;
    private int stock;
    private ItemDto generalItem;

    public ItemTypeDto(ItemType itemType) {
        this.size = itemType.getSize();
        this.id = itemType.getId();
        this.color = itemType.getColor();
        this.stock = itemType.getStock();
        this.generalItem = new ItemDto(itemType.getGeneralItem());
    }

    @SuppressWarnings("unused")
    public ItemTypeDto() {
    }

    // Getter methods
    public Size getSize() {
        return size;
    }
    public int getId() {
        return id;
    }
    public Color getColor() {
        return color;
    }
    public int getStock() {
        return stock;
    }
    public ItemDto getGeneralItem() {
        return generalItem;
}
}
