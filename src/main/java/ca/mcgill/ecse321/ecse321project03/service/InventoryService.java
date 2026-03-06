package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Employee;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.model.Role;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemTypeRepository;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class InventoryService {

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Restock inventory for a specific ItemType by increasing its stock.
     * This corresponds to the "Restock Inventory" use case.
     *
     * @author Yani Zhang
     * @param itemTypeId the id of the ItemType to restock
     * @param quantityToAdd the quantity to add (must be positive)
     * @param accountId Account who is managing the inventory
     * @return the updated ItemType
     */
    @Transactional
    public ItemType restockItemType(int itemTypeId, int quantityToAdd, int accountId) {

        // Verify that it's the manager who restores the stock
        if (accountRepository.findById(accountId).isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account managing the stock found with id " + accountId);
        }
        
        if (roleRepository.findByAccount_Id(accountId) == null) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "A customer cannot manage the stock");
        } else if (roleRepository.findByAccount_Id(accountId) instanceof Employee) {
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "An employee cannot manage the stock");
        }


        if (quantityToAdd <= 0) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Restock quantity must be positive");
        }

        ItemType itemType = itemTypeRepository.findById(itemTypeId).orElse(null);
        if (itemType == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No item type found with id " + itemTypeId);
        }

        itemType.setStock(itemType.getStock() + quantityToAdd);
        return itemTypeRepository.save(itemType);
    }

    /**
     * View an ItemType's stock status as a human-readable label.
     * This corresponds to the UI requirement for Customer/Employee/Manager to see:
     * - "In Stock"
     * - "Almost Out of Stock! Only X left!!!" (when stock is less than 10)
     * - "Out of Stock"
     *
     * @author Yani Zhang
     * @param accountId Account verifying the status
     * @param itemTypeId the id of the ItemType
     * @return a stock status label for display
     */
    @Transactional
    public String getItemTypeStockStatus(int itemTypeId, int accountId) {
        // Verify that the account verifying the status exists
        if (accountRepository.findById(accountId).isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with id " + accountId);
        }

        ItemType itemType = itemTypeRepository.findById(itemTypeId).orElse(null);
        if (itemType == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No item type found with id " + itemTypeId);
        }

        int stock = itemType.getStock();

        if (stock <= 0) {
            return "Out of Stock";
        }
        if (stock < 10) {
            return "Almost Out of Stock! Only " + stock + " left!!!";
        }
        return "In Stock";
    }


    /**
     * View the inventory level of a specific ItemType (specific size and color).
     * This corresponds to the "View Item's Inventory Level" use case.
     * Unlike stock status (which shows a label such as "In Stock"), this method
     * returns the exact stock number of a specific ItemType.
     *
     * @author Yani Zhang
     * @param accountId account verifying the level
     * @param itemTypeId the id of the ItemType
     * @return the exact inventory level (stock) of the ItemType
     */
    @Transactional
    public int getInventoryLevel(int itemTypeId, int accountId) {

        // Verify that the account verifying the inventory level exists
        if (accountRepository.findById(accountId).isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No account found with id " + accountId);
        }

        ItemType itemType = itemTypeRepository.findById(itemTypeId).orElse(null);
        if (itemType == null) {
            throw new Ecse321Project03Exception(HttpStatus.NOT_FOUND,
                    "No item type found with id " + itemTypeId);
        }
        return itemType.getStock();
    }

    /**
     * Utility: list all ItemTypes (useful for inventory screens).
     *
     * @author Yani Zhang
     * @return list of all ItemTypes
     */
    @Transactional
    public List<ItemType> getAllItemTypes() {
        List<ItemType> result = new ArrayList<>();
        for (ItemType it : itemTypeRepository.findAll()) {
            result.add(it);
        }
        return result;
    }
}
