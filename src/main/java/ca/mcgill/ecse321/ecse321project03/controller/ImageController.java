package ca.mcgill.ecse321.ecse321project03.controller;

import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemImageDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.ItemImage;
import ca.mcgill.ecse321.ecse321project03.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for handling item image upload operations.
 * Connects HTTP requests to the ImageService layer.
 *
 * @author Yani Zhang
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * Uploads images for a specific item.
     * Only managers are allowed to upload item images.
     *
     * @param itemId the id of the item
     * @param accountId the id of the account performing the upload
     * @param files list of image files to upload
     * @return a ResponseEntity containing the list of uploaded ItemImageDto objects
     *         or an ErrorDto if the upload fails
     */
    @PostMapping("/items/{itemId}")
    public ResponseEntity<?> uploadItemImages(@PathVariable int itemId, @RequestParam int accountId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            List<ItemImage> images = imageService.uploadItemImages(accountId, itemId, files);

            List<ItemImageDto> imageDtos = images.stream()
                    .map(ItemImageDto::new)
                    .toList();

            return new ResponseEntity<>(imageDtos, HttpStatus.CREATED);

        } catch (Ecse321Project03Exception e) {
            return new ResponseEntity<>(new ErrorDto(e.getMessage()), e.getStatus());
        }
    }
}
