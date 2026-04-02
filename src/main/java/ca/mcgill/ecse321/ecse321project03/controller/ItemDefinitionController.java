package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemDefinitionResponseDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.ItemImage;
import ca.mcgill.ecse321.ecse321project03.model.ItemType;
import ca.mcgill.ecse321.ecse321project03.service.ImageService;
import ca.mcgill.ecse321.ecse321project03.service.ItemDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for item definition (create item with optional images).
 * 
 * It connects the user interface to the item definition service and image service.
 * Endpoints: create, update, and delete items (with optional image upload on create/update).
 */
@RestController
@RequestMapping("/items")
public class ItemDefinitionController {

    private static final String IMAGE_URL_PREFIX = "/images/";

    @Autowired
    private ItemDefinitionService itemDefinitionService;

    @Autowired
    private ImageService imageService;

    /**
     * Creates a new item with variants and optionally uploads images.
     *
     * @param accountId      the authenticated account (must be a manager)
     * @param itemCreationDto the item and variant specs (part name "item")
     * @param files           optional image files (part name "files")
     * @return ItemDefinitionResponseDto with item fields, variants, and image URLs for the frontend
     * @author Zirun Wang
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createItem(
            @RequestParam int accountId,
            @RequestPart("item") ItemCreationDto itemCreationDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            Item item = itemDefinitionService.createItem(accountId, itemCreationDto);
            List<String> imageUrls = Collections.emptyList();

            if (files != null && !files.isEmpty()) {
                List<ItemImage> images = imageService.uploadItemImages(accountId, item.getId(), files);
                imageUrls = images.stream()
                        .map(im -> IMAGE_URL_PREFIX + im.getKey())
                        .collect(Collectors.toList());
            }

            List<ItemType> variants = itemDefinitionService.getItemTypesForItem(item.getId());
            ItemDefinitionResponseDto response = new ItemDefinitionResponseDto(item, variants, imageUrls);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Updates an existing item by id using the same payload as creation.
     * When {@code files} is non-empty, all previous images for the item are removed and replaced by the upload.
     *
     * @param accountId       the authenticated account (must be a manager)
     * @param itemId          the item id
     * @param itemCreationDto the new item and variant specs (part name "item")
     * @param files           optional replacement images (part name "files")
     * @return item definition response with updated variants and image URLs
     * @author Zirun Wang
     */
    @PutMapping(value = "/{itemId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateItem(
            @RequestParam int accountId,
            @PathVariable int itemId,
            @RequestPart("item") ItemCreationDto itemCreationDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            Item item = itemDefinitionService.updateItem(accountId, itemId, itemCreationDto, files);
            List<String> imageUrls = imageService.findImagesForItem(item.getId()).stream()
                    .map(im -> IMAGE_URL_PREFIX + im.getKey())
                    .collect(Collectors.toList());
            List<ItemType> variants = itemDefinitionService.getItemTypesForItem(item.getId());
            ItemDefinitionResponseDto response = new ItemDefinitionResponseDto(item, variants, imageUrls);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }

    /**
     * Deletes an item by id, including its variants, cart/order lines for those variants, and images.
     *
     * @param accountId the authenticated account (must be a manager)
     * @param itemId    the item id
     * @return 204 No Content on success
     * @author Zirun Wang
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@RequestParam int accountId, @PathVariable int itemId) {
        try {
            itemDefinitionService.deleteItem(accountId, itemId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }
}
