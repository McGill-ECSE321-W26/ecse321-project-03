package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.dto.ItemCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemCreationDto.VariantSpec;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.Item.Tag;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemTypeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class ItemDefinitionService {

    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int INITIAL_STOCK_MAX = 100_000;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    /**
     * Creates a new item and its variants. Manager-only.
     *
     * @param accountId authenticated account id (must be a manager)
     * @param dto       item and variant specs
     * @return the created and persisted Item
     */
    @Transactional
    public Item createItem(int accountId, ItemCreationDto dto) {
        ensureManager(accountId);
        validateItemFields(dto);
        validateVariants(dto);
        checkItemNameUnique(dto.getName());
        Item item = persistItem(dto);
        persistVariants(item, dto.getVariants());
        return item;
    }

    /**
     * Returns all item types (variants) for the given item.
     *
     * @param itemId the item id
     * @return list of ItemType for that item (empty if none)
     */
    public List<ItemType> getItemTypesForItem(int itemId) {
        return itemTypeRepository.findByGeneralItem_Id(itemId);
    }

    private void ensureManager(int accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new Ecse321Project03Exception(HttpStatus.UNAUTHORIZED, "User not logged in.");
        }
        Manager manager = managerRepository.findByAccount_Id(accountId);
        if (manager == null) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Only managers can create items.");
        }
    }

    private void validateItemFields(ItemCreationDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "name: must not be null or blank.");
        }
        if (dto.getName().length() < NAME_MIN_LENGTH || dto.getName().length() > NAME_MAX_LENGTH) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                    "name: length must be between " + NAME_MIN_LENGTH + " and " + NAME_MAX_LENGTH + ".");
        }
        Tag tag = parseTag(dto.getTag());
        if (tag == null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "tag: must be a valid enum value.");
        }
        if (dto.getPrice() == null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "price: must not be null.");
        }
        if (dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "price: must be >= 0.");
        }
    }

    private void validateVariants(ItemCreationDto dto) {
        if (dto.getVariants() == null) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "variants: must not be null.");
        }
        if (dto.getVariants().isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "variants: must not be empty.");
        }
        Set<String> seenPairs = new HashSet<>();
        List<VariantSpec> variants = dto.getVariants();
        for (int i = 0; i < variants.size(); i++) {
            VariantSpec v = variants.get(i);
            String prefix = "variants[" + i + "].";
            if (v.getColor() == null || v.getColor().isBlank()) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, prefix + "color: must not be null.");
            }
            ItemType.Color color = parseColor(v.getColor());
            if (color == null) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, prefix + "color: invalid value.");
            }
            if (v.getSize() == null || v.getSize().isBlank()) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, prefix + "size: must not be null.");
            }
            ItemType.Size size = parseSize(v.getSize());
            if (size == null) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, prefix + "size: invalid value.");
            }
            if (v.getInitialStock() == null) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, prefix + "initialStock: must not be null.");
            }
            if (v.getInitialStock() < 0) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, prefix + "initialStock: must be >= 0.");
            }
            if (v.getInitialStock() > INITIAL_STOCK_MAX) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                        prefix + "initialStock: must be at most " + INITIAL_STOCK_MAX + ".");
            }
            String pair = color.name() + "/" + size.name();
            if (!seenPairs.add(pair)) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                        "duplicate variant " + color.name() + "/" + size.name() + ".");
            }
        }
    }

    private void checkItemNameUnique(String name) {
        List<Item> existing = itemRepository.findByName(name);
        if (existing != null && !existing.isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.CONFLICT, "Item name already exists: " + name);
        }
    }

    private Item persistItem(ItemCreationDto dto) {
        Tag tag = parseTag(dto.getTag());
        double price = dto.getPrice().doubleValue();
        Item item = new Item(dto.getName().trim(), tag, price);
        return itemRepository.save(item);
    }

    private List<ItemType> persistVariants(Item item, List<VariantSpec> specs) {
        return specs.stream()
                .map(s -> {
                    ItemType.Color color = parseColor(s.getColor());
                    ItemType.Size size = parseSize(s.getSize());
                    ItemType type = new ItemType(size, color, s.getInitialStock(), item);
                    return itemTypeRepository.save(type);
                })
                .toList();
    }

    private static Tag parseTag(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        for (Tag t : Tag.values()) {
            if (t.name().equalsIgnoreCase(normalized)) return t;
        }
        return null;
    }

    private static ItemType.Color parseColor(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        for (ItemType.Color c : ItemType.Color.values()) {
            if (c.name().equalsIgnoreCase(normalized)) return c;
        }
        return null;
    }

    private static ItemType.Size parseSize(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        for (ItemType.Size s : ItemType.Size.values()) {
            if (s.name().equalsIgnoreCase(normalized)) return s;
        }
        return null;
    }
}
