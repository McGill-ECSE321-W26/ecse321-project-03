package ca.mcgill.ecse321.ecse321project03.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * ItemCreationDto is a DTO for creating a new item.
 * It contains the name, tag, price, and variants of the item.
 * The variants are a list of VariantSpec objects, each containing a color, size, and initial stock.
 * 
 * @author Zirun Wang   
 */
public class ItemCreationDto {

    private String name;
    private String tag;
    private BigDecimal price;
    private List<VariantSpec> variants;

    @SuppressWarnings("unused")
    public ItemCreationDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<VariantSpec> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantSpec> variants) {
        this.variants = variants;
    }

    public static class VariantSpec {
        private String color;
        private String size;
        private Integer initialStock;

        @SuppressWarnings("unused")
        public VariantSpec() {
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public Integer getInitialStock() {
            return initialStock;
        }

        public void setInitialStock(Integer initialStock) {
            this.initialStock = initialStock;
        }
    }
}
