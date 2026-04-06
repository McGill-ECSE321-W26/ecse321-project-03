package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.ItemImageDto;
import ca.mcgill.ecse321.ecse321project03.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller responsible for handling item-image-related API endpoints.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/items")
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * Uploads one or more images for an item. Manager-only.
     *
     * @param accountId the authenticated account id
     * @param itemId the id of the item
     * @param files the image files to upload
     * @return the list of uploaded item images
     */
    @PostMapping("/{itemId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ItemImageDto> uploadItemImages(@RequestParam int accountId,
                                               @PathVariable int itemId,
                                               @RequestParam("files") List<MultipartFile> files) {
        return imageService.uploadItemImages(accountId, itemId, files)
                .stream()
                .map(ItemImageDto::new)
                .toList();
    }

    /**
     * Replaces all images for an item with a new set of uploaded images. Manager-only.
     *
     * @param accountId the authenticated account id
     * @param itemId the id of the item
     * @param files the new image files
     * @return the list of newly uploaded item images
     */
    @PutMapping("/{itemId}/images")
    public List<ItemImageDto> replaceItemImages(@RequestParam int accountId,
                                                @PathVariable int itemId,
                                                @RequestParam("files") List<MultipartFile> files) {
        return imageService.replaceItemImages(accountId, itemId, files)
                .stream()
                .map(ItemImageDto::new)
                .toList();
    }

    /**
     * Deletes all images associated with an item. Manager-only.
     *
     * @param accountId the authenticated account id
     * @param itemId the id of the item
     */
    @DeleteMapping("/{itemId}/images")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllItemImages(@RequestParam int accountId,
                                    @PathVariable int itemId) {
        imageService.deleteAllItemImages(accountId, itemId);
    }

    /**
     * Retrieves all persisted images for an item.
     *
     * @param itemId the id of the item
     * @return the list of item images associated with the item
     */
    @GetMapping("/{itemId}/images")
    public List<ItemImageDto> findImagesForItem(@PathVariable int itemId) {
        return imageService.findImagesForItem(itemId)
                .stream()
                .map(ItemImageDto::new)
                .toList();
    }
}
