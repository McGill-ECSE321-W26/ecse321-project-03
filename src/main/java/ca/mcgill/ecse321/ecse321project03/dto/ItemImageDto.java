package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.ItemImage;

/**
 * Data Transfer Object for ItemImage.
 * Used to transfer image metadata without exposing the entity.
 *
 * @author Yani Zhang
 */
public class ItemImageDto {

    private int id;
    private String key;
    private int itemId;

    /**
     * Default constructor.
     */
    public ItemImageDto() {
    }

    /**
     * Constructor converting a model object to DTO.
     *
     * @param image the ItemImage model object
     */
    public ItemImageDto(ItemImage image) {
        this.id = image.getId();
        this.key = image.getKey();
        this.itemId = image.getItem().getId();
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public int getItemId() {
        return itemId;
    }
}