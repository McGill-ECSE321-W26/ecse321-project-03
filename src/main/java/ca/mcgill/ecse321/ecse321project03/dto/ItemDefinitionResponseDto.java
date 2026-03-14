package ca.mcgill.ecse321.ecse321project03.dto;

import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.Item.Tag;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for item creation. Contains item fields, variant info, and
 * image URLs that the frontend can use to load and render images (e.g. via
 * GET /images/... endpoints).
 */
public class ItemDefinitionResponseDto {

    private int id;
    private String name;
    private Tag tag;
    private double price;
    private List<VariantResponse> variants;
    private List<String> imageUrls;

    /**
     * Builds the response from an Item, its types, and a list of image URL paths.
     * Image URLs should be paths like "/images/items/1/uuid.jpg" that the frontend
     * can use to load images.
     */
    public ItemDefinitionResponseDto(Item item, List<ItemType> itemTypes, List<String> imageUrls) {
        this.id = item.getId();
        this.name = item.getName();
        this.tag = item.getTag();
        this.price = item.getPrice();
        this.variants = itemTypes == null ? List.of() : itemTypes.stream()
                .map(VariantResponse::new)
                .collect(Collectors.toList());
        this.imageUrls = imageUrls == null ? Collections.emptyList() : List.copyOf(imageUrls);
    }

    @SuppressWarnings("unused")
    public ItemDefinitionResponseDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<VariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantResponse> variants) {
        this.variants = variants;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public static class VariantResponse {
        private int id;
        private ItemType.Color color;
        private ItemType.Size size;
        private int stock;

        public VariantResponse(ItemType itemType) {
            this.id = itemType.getId();
            this.color = itemType.getColor();
            this.size = itemType.getSize();
            this.stock = itemType.getStock();
        }

        @SuppressWarnings("unused")
        public VariantResponse() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public ItemType.Color getColor() {
            return color;
        }

        public void setColor(ItemType.Color color) {
            this.color = color;
        }

        public ItemType.Size getSize() {
            return size;
        }

        public void setSize(ItemType.Size size) {
            this.size = size;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }
}
