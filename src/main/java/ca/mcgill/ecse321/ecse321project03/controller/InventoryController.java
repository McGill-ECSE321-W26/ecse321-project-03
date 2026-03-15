package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemTypeDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller responsible for handling inventory-related API endpoints.
 * It connects HTTP requests from the client to the InventoryService layer.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Restocks a specific ItemType by increasing its stock quantity.
     *
     * @param itemTypeId the id of the ItemType to restock
     * @param quantityToAdd the quantity to add to stock
     * @param accountId the id of the account managing the inventory
     * @return a ResponseEntity containing the updated ItemTypeDto if successful,
     *         or an ErrorDto if restocking fails
     */
    @PostMapping("/restock/{itemTypeId}")
    public ItemTypeDto restockItemType(
            @PathVariable int itemTypeId,
            @RequestParam int quantityToAdd,
            @RequestParam int accountId) {
            ItemType itemType = inventoryService.restockItemType(itemTypeId, quantityToAdd, accountId);
            return new ItemTypeDto(itemType);

    }

    /**
     * Retrieves the stock status label of a specific ItemType.
     *
     * @param itemTypeId the id of the ItemType
     * @param accountId the id of the account viewing the stock status
     * @return a ResponseEntity containing the stock status string if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/stock-status/{itemTypeId}")
    public String getItemTypeStockStatus(
            @PathVariable int itemTypeId,
            @RequestParam int accountId) {
            String stockStatus = inventoryService.getItemTypeStockStatus(itemTypeId, accountId);
            return stockStatus;

    }

    /**
     * Retrieves the exact inventory level of a specific ItemType.
     *
     * @param itemTypeId the id of the ItemType
     * @param accountId the id of the account viewing the inventory level
     * @return a ResponseEntity containing the stock level if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/stock-level/{itemTypeId}")
    public int getInventoryLevel(
            @PathVariable int itemTypeId,
            @RequestParam int accountId) {
            int stockLevel = inventoryService.getInventoryLevel(itemTypeId, accountId);
            return stockLevel;

    }

    /**
     * Retrieves all ItemTypes in the inventory.
     *
     * @return a ResponseEntity containing a list of ItemTypeDto objects if successful,
     *         or an ErrorDto if the request fails
     */
    @GetMapping("/item-types")
    public  List<ItemTypeDto> getAllItemTypes(@RequestParam int accountId) {
            List<ItemType> itemTypes = inventoryService.getAllItemTypes(accountId);
            List<ItemTypeDto> itemTypeDtos = itemTypes.stream()
                    .map(ItemTypeDto::new)
                    .toList();
            return itemTypeDtos;
    }
}
