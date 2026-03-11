package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemResponseDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.service.ItemCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for handling item-creation API endpoints.
 * It connects HTTP requests from the client to the ItemCreationService layer.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/items")
public class ItemCreationController {

    @Autowired
    private ItemCreationService itemCreationService;

    /**
     * Creates a new item and its associated variants.
     *
     * @param accountId the ID of the authenticated account creating the item
     * @param itemCreationDto the DTO containing the item information and variant specifications
     * @return a ResponseEntity containing the created ItemResponseDto if successful,
     *         or an ErrorDto if item creation fails
     */
    @PostMapping
    public ResponseEntity<?> createItem(@RequestParam int accountId, @RequestBody ItemCreationDto itemCreationDto) {
        try {
            ItemResponseDto createdItem = itemCreationService.createItem(accountId, itemCreationDto);
            return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }
}
