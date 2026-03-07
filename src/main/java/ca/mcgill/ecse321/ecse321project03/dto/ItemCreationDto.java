package ca.mcgill.ecse321.ecse321project03.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Expected Request Body:
{
  "name": "Classic Tee",
  "tag": "TOP",
  "price": 19.99,
  "variants": [
    { "color": "Black", "size": "S", "initialStock": 10 },
    { "color": "Black", "size": "M", "initialStock": 5 },
    { "color": "White", "size": "S", "initialStock": 7 }
  ]
}
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

    /**
     * One variant spec: color + size + initial stock. Matches ItemType.Color, ItemType.Size and stock.
     */
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
