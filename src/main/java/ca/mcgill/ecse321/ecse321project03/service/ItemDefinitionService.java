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
import ca.mcgill.ecse321.ecse321project03.repository.CartItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemTypeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;
import ca.mcgill.ecse321.ecse321project03.repository.OrderItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Service for creating, updating, deleting, and querying item definitions (items and their variants).
 * Mutations are manager-only; validation includes name, tag, price, and variant specs (color, size, initial stock).
 *
 * @author Zirun Wang
 */
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

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ImageService imageService;

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
     * Updates an existing item and its variants from the DTO (full overwrite of variants).
     * Removed variants are deleted (after clearing cart/order lines for those types).
     * If {@code files} is non-null and non-empty, existing images are removed from DB and disk and replaced by the upload.
     * Manager-only.
     *
     * @param accountId authenticated account id (must be a manager)
     * @param itemId    id of the item to update
     * @param dto       new item and variant specs
     * @param files     optional new images; when present and non-empty, replaces all item images
     * @return the updated persisted Item
     */
    @Transactional
    public Item updateItem(int accountId, int itemId, ItemCreationDto dto, List<MultipartFile> files) {
        ensureManager(accountId);
        validateItemFields(dto);
        validateVariants(dto);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                        "No item found with id " + itemId));
        checkItemNameUniqueForUpdate(itemId, dto.getName());
        item.setName(dto.getName().trim());
        item.setTag(parseTag(dto.getTag()));
        item.setPrice(dto.getPrice().doubleValue());
        itemRepository.save(item);
        syncVariants(item, dto.getVariants());
        if (files != null && !files.isEmpty()) {
            imageService.replaceItemImages(accountId, itemId, files);
        }
        return item;
    }

    /**
     * Deletes an item and all dependent data: cart lines and order lines for its variants,
     * item types, images (DB and files), then the item. Manager-only.
     *
     * @param accountId authenticated account id (must be a manager)
     * @param itemId    id of the item to delete
     */
    @Transactional
    public void deleteItem(int accountId, int itemId) {
        ensureManager(accountId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                        "No item found with id " + itemId));
        List<ItemType> types = itemTypeRepository.findByGeneralItem_Id(itemId);
        for (ItemType type : types) {
            removeCartAndOrderItemsForItemType(type.getId());
        }
        for (ItemType type : types) {
            itemTypeRepository.delete(type);
        }
        imageService.deleteAllItemImages(accountId, itemId);
        itemRepository.delete(item);
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

    /**
     * Returns all items 
     *
     * @param accountId the account id 
     * @return list of all items 
     */
    public List<Item> getAllItems(int accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new Ecse321Project03Exception(HttpStatus.UNAUTHORIZED, "User not logged in.");
        }
        List<Item> items = new ArrayList<>();
        itemRepository.findAll().forEach(items::add);
        return items;
    }

    /**
     * Verifies that the given account exists and is associated with a manager.
     * Throws if the account is missing or the user is not a manager.
     *
     * @param accountId the authenticated account id
     */
    private void ensureManager(int accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new Ecse321Project03Exception(HttpStatus.UNAUTHORIZED, "User not logged in.");
        }
        Manager manager = managerRepository.findByAccount_Id(accountId);
        if (manager == null) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Only managers can manage items.");
        }
    }

    /**
     * Validates item-level fields: name (non-blank, length within bounds), tag (valid enum), and price (non-null, &gt;= 0).
     *
     * @param dto the item creation DTO to validate
     */
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

    /**
     * Validates the variants list: non-null, non-empty; each variant has valid color, size, and initialStock; no duplicate color/size pairs.
     *
     * @param dto the item creation DTO containing variants to validate
     */
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

    /**
     * Ensures no existing item has the given name; throws CONFLICT if the name is already used.
     *
     * @param name the item name to check for uniqueness
     */
    private void checkItemNameUnique(String name) {
        List<Item> existing = itemRepository.findByName(name);
        if (existing != null && !existing.isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.CONFLICT, "Item name already exists: " + name);
        }
    }

    /**
     * Ensures no <em>other</em> item uses the given name when updating {@code itemId}.
     *
     * @param itemId id of the item being updated
     * @param name   new name
     */
    private void checkItemNameUniqueForUpdate(int itemId, String name) {
        List<Item> existing = itemRepository.findByName(name);
        if (existing == null || existing.isEmpty()) {
            return;
        }
        for (Item other : existing) {
            if (other.getId() != itemId) {
                throw new Ecse321Project03Exception(HttpStatus.CONFLICT, "Item name already exists: " + name);
            }
        }
    }

    /**
     * Aligns persisted variants with the DTO: updates stock for matching color/size, creates new rows,
     * removes types no longer listed (after clearing cart/order references).
     */
    private void syncVariants(Item item, List<VariantSpec> specs) {
        List<ItemType> existing = itemTypeRepository.findByGeneralItem_Id(item.getId());
        Map<String, ItemType> byPair = new HashMap<>();
        if (existing != null) {
            for (ItemType t : existing) {
                byPair.put(t.getColor().name() + "/" + t.getSize().name(), t);
            }
        }
        Set<String> dtoPairs = new HashSet<>();
        for (VariantSpec s : specs) {
            ItemType.Color color = parseColor(s.getColor());
            ItemType.Size size = parseSize(s.getSize());
            String pair = color.name() + "/" + size.name();
            dtoPairs.add(pair);
            ItemType current = byPair.get(pair);
            if (current != null) {
                current.setStock(s.getInitialStock());
                itemTypeRepository.save(current);
            } else {
                ItemType created = new ItemType(size, color, s.getInitialStock(), item);
                itemTypeRepository.save(created);
            }
        }
        if (existing != null) {
            for (ItemType t : existing) {
                String pair = t.getColor().name() + "/" + t.getSize().name();
                if (!dtoPairs.contains(pair)) {
                    removeCartAndOrderItemsForItemType(t.getId());
                    itemTypeRepository.delete(t);
                }
            }
        }
    }

    private void removeCartAndOrderItemsForItemType(int itemTypeId) {
        cartItemRepository.findByCartItemId_ItemType_Id(itemTypeId).forEach(cartItemRepository::delete);
        orderItemRepository.findByOrderItemId_ItemType_Id(itemTypeId).forEach(orderItemRepository::delete);
    }

    /**
     * Creates and persists a single Item from the DTO (name, tag, price).
     *
     * @param dto the item creation DTO
     * @return the saved Item entity
     */
    private Item persistItem(ItemCreationDto dto) {
        Tag tag = parseTag(dto.getTag());
        double price = dto.getPrice().doubleValue();
        Item item = new Item(dto.getName().trim(), tag, price);
        return itemRepository.save(item);
    }

    /**
     * Creates and persists ItemType variants for the given item from the variant specs.
     *
     * @param item  the parent item
     * @param specs the variant specifications (color, size, initialStock)
     * @return list of saved ItemType entities
     */
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

    /**
     * Parses a string into an Item.Tag enum value (case-insensitive). Returns null if null, blank, or invalid.
     *
     * @param value the string to parse
     * @return the matching Tag or null
     */
    private static Tag parseTag(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        for (Tag t : Tag.values()) {
            if (t.name().equalsIgnoreCase(normalized)) return t;
        }
        return null;
    }

    /**
     * Parses a string into an ItemType.Color enum value (case-insensitive). Returns null if null, blank, or invalid.
     *
     * @param value the string to parse
     * @return the matching Color or null
     */
    private static ItemType.Color parseColor(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        for (ItemType.Color c : ItemType.Color.values()) {
            if (c.name().equalsIgnoreCase(normalized)) return c;
        }
        return null;
    }

    /**
     * Parses a string into an ItemType.Size enum value (case-insensitive). Returns null if null, blank, or invalid.
     *
     * @param value the string to parse
     * @return the matching Size or null
     */
    private static ItemType.Size parseSize(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        for (ItemType.Size s : ItemType.Size.values()) {
            if (s.name().equalsIgnoreCase(normalized)) return s;
        }
        return null;
    }

        /**
     * Returns a single item by id.
     *
     * @param itemId the item id
     * @return the Item entity
     */
    public Item getItemById(int itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                        "No item found with id " + itemId));
    }
}
