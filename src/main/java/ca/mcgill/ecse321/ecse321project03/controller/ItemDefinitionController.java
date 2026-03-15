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
import org.springframework.web.bind.annotation.PostMapping;
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
 * Single endpoint: create item and optionally upload images in one request.
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
     * Creates a new item with variants and optionally uploads images in one request.
     * Request must be multipart/form-data with:
     * - "item" (application/json): item name, tag, price, variants
     * - "files" (optional): image files (e.g. image/jpeg, image/png, image/webp)
     *
     * @param accountId      the authenticated account (must be a manager)
     * @param itemCreationDto the item and variant specs (part name "item")
     * @param files           optional image files (part name "files")
     * @return ItemDefinitionResponseDto with item fields, variants, and image URLs for the frontend
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
}
