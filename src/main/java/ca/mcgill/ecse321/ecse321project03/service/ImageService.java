package ca.mcgill.ecse321.ecse321project03.service;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.ItemImage;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemImageRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Service for uploading and managing item images. Handles validation of file type and size,
 * persistence of image metadata, and writing image files to disk under an uploads directory.
 *
 * @author Zirun Wang
 */
@Service
public class ImageService {

    private static final String UPLOADS_BASE = "uploads";
    private static final String KEY_PREFIX = "items";
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
    private static final int MAX_FILES = 10;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemImageRepository itemImageRepository;

    /**
     * Uploads images for an item. Manager-only. Validates count, content type, and size; saves to DB then to disk.
     *
     * @param accountId authenticated account (must be manager)
     * @param itemId    existing item id
     * @param files     image files (max 10, each max 5MB, jpeg/png/webp)
     * @return list of persisted ItemImage entities
     */
    @Transactional
    public List<ItemImage> uploadItemImages(int accountId, int itemId, List<MultipartFile> files) {
        ensureManager(accountId);
        Item item = getItemOrThrow(itemId);
        return uploadItemImagesForItem(item, itemId, files);
    }

    /**
     * Replaces all images for an item: deletes existing DB rows and files, then uploads the given files.
     * Manager-only. At least one file is required (same validation as upload).
     *
     * @param accountId authenticated account (must be manager)
     * @param itemId    existing item id
     * @param files     new image files
     * @return list of persisted ItemImage entities
     */
    @Transactional
    public List<ItemImage> replaceItemImages(int accountId, int itemId, List<MultipartFile> files) {
        ensureManager(accountId);
        Item item = getItemOrThrow(itemId);
        deleteAllItemImageRecordsAndFiles(itemId);
        return uploadItemImagesForItem(item, itemId, files);
    }

    /**
     * Deletes all image metadata and files on disk for the given item. Manager-only.
     *
     * @param accountId authenticated account (must be manager)
     * @param itemId    existing item id
     */
    @Transactional
    public void deleteAllItemImages(int accountId, int itemId) {
        ensureManager(accountId);
        getItemOrThrow(itemId);
        deleteAllItemImageRecordsAndFiles(itemId);
    }

    /**
     * Returns all persisted images for an item (no authorization).
     *
     * @param itemId the item id
     * @return list of ItemImage, never null
     */
    public List<ItemImage> findImagesForItem(int itemId) {
        List<ItemImage> list = itemImageRepository.findByItem_Id(itemId);
        return list == null ? List.of() : list;
    }

    private List<ItemImage> uploadItemImagesForItem(Item item, int itemId, List<MultipartFile> files) {
        validateFiles(files);
        List<ImageRecord> records = buildImageRecords(itemId, files);
        List<ItemImage> saved = persistImageRecords(item, records);
        writeFilesToDisk(records);
        return saved;
    }

    private void deleteAllItemImageRecordsAndFiles(int itemId) {
        List<ItemImage> images = itemImageRepository.findByItem_Id(itemId);
        if (images == null) {
            return;
        }
        for (ItemImage im : images) {
            try {
                Files.deleteIfExists(pathFromKey(im.getKey()));
            } catch (IOException ignored) {
            }
            itemImageRepository.delete(im);
        }
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
            throw new Ecse321Project03Exception(HttpStatus.FORBIDDEN, "Only managers can upload item images.");
        }
    }

    /**
     * Loads the item by id or throws if not found.
     *
     * @param itemId the item id
     * @return the Item entity
     */
    private Item getItemOrThrow(int itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No item found with id " + itemId));
    }

    /**
     * Validates the list of uploaded files: non-null, non-empty, count within limit,
     * each file non-empty, allowed content type (jpeg/png/webp), and size at most 5MB.
     *
     * @param files the list of multipart files to validate
     */
    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "At least one image file is required.");
        }
        if (files.size() > MAX_FILES) {
            throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                    "Maximum " + MAX_FILES + " files allowed, got " + files.size() + ".");
        }
        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            if (f == null || f.isEmpty()) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "files[" + i + "]: file must not be empty.");
            }
            String contentType = f.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                        "files[" + i + "]: content type must be one of image/jpeg, image/png, image/webp.");
            }
            if (f.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST,
                        "files[" + i + "]: size must not exceed 5MB.");
            }
        }
    }

    /**
     * Builds image records with a unique key per file. Key format: items/{itemId}/{uuid}.{ext}.
     *
     * @param itemId the item id used in the path
     * @param files  the uploaded files
     * @return list of ImageRecord with key and byte content
     */
    private List<ImageRecord> buildImageRecords(int itemId, List<MultipartFile> files) {
        List<ImageRecord> records = new ArrayList<>();
        for (MultipartFile f : files) {
            String ext = extensionFromContentType(f.getContentType());
            String key = KEY_PREFIX + "/" + itemId + "/" + UUID.randomUUID() + "." + ext;
            try {
                records.add(new ImageRecord(key, f.getBytes()));
            } catch (IOException e) {
                throw new Ecse321Project03Exception(HttpStatus.BAD_REQUEST, "Failed to read file: " + e.getMessage());
            }
        }
        return records;
    }

    /**
     * Persists image metadata for each record as ItemImage entities linked to the given item.
     *
     * @param item    the item to associate images with
     * @param records the image records (key and bytes)
     * @return list of saved ItemImage entities
     */
    private List<ItemImage> persistImageRecords(Item item, List<ImageRecord> records) {
        List<ItemImage> saved = new ArrayList<>();
        for (ImageRecord r : records) {
            ItemImage im = new ItemImage(r.key, item);
            saved.add(itemImageRepository.save(im));
        }
        return saved;
    }

    /**
     * Writes each image record's bytes to disk under the uploads directory. On failure,
     * removes any files already written.
     *
     * @param records the image records to write
     */
    private void writeFilesToDisk(List<ImageRecord> records) {
        List<Path> written = new ArrayList<>();
        try {
            for (ImageRecord r : records) {
                Path path = Path.of(UPLOADS_BASE, r.key);
                Files.createDirectories(path.getParent());
                Files.write(path, r.bytes);
                written.add(path);
            }
        } catch (IOException e) {
            // If an error occurs during writing to disk, all previously written files will be deleted
            deletePathsQuietly(written);
            throw new Ecse321Project03Exception(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to write image to disk: " + e.getMessage());
        }
    }

    /**
     * Deletes the given paths from the filesystem, ignoring any IO errors.
     *
     * @param paths the paths to delete
     */
    public static void deletePathsQuietly(List<Path> paths) {
        for (Path p : paths) {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Maps a content type to a file extension (jpg, png, or webp). Defaults to "jpg" if null or unknown.
     *
     * @param contentType the MIME content type
     * @return the corresponding file extension
     */
    private static String extensionFromContentType(String contentType) {
        if (contentType == null) return "jpg"; //default to jpg
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    /**
     * Resolves the filesystem path for an image key (e.g. for serving or deletion).
     * Path is relative to project/process working directory: uploads/items/{itemId}/{uuid}.{ext}
     *
     * @param key the image key (e.g. items/1/uuid.png)
     * @return the Path relative to the uploads base directory
     */
    public static Path pathFromKey(String key) {
        return Path.of(UPLOADS_BASE, key);
    }

    /**
     * Holds the storage key and raw bytes for an image before persistence and disk write.
     */
    private static class ImageRecord {
        final String key;
        final byte[] bytes;

        /**
         * Creates an image record with the given key and byte content.
         *
         * @param key   the storage key (path under uploads)
         * @param bytes the image bytes
         */
        ImageRecord(String key, byte[] bytes) {
            this.key = key;
            this.bytes = bytes;
        }
    }
}
