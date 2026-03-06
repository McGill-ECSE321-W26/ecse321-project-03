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
 * ImageService
 * uploadItemImages(int accountId, int itemId, List<MultipartFile> files)
 * Returns List<ItemImage> (entities only; DTO mapping in controller).
 * 
 * - Authorization: 
 * ensureManager(accountId) → 401 if no account, 403 if not manager.
 * - Item exists: 
 * getItemOrThrow(itemId) → 404 if item not found.
 * - Validation (400):
 * files not null and not empty.
 * At most 10 files.
 * Each file: content type in image/jpeg, image/png, image/webp; size ≤ 5MB; non-empty.
 * - Build records: 
 * for each file, key items/{itemId}/{uuid}.{ext} and bytes from MultipartFile.getBytes().
 * - Transactional: 
 * persist all ItemImage rows (same order as files), then write bytes to disk under uploads/.
 * on write failure, delete any files already written in this request, then throw 500.
 * - Design choices:
 * DB then disk: Persist ItemImage first, then write files. If a file write fails, the transaction rolls back (no orphan DB rows); any files already written are removed in a catch block.
 * Single transaction: One @Transactional method so all inserts commit together; file I/O is best-effort cleanup on failure.
 * pathFromKey(String key): Static helper so controllers (or other code) can resolve key → Path for serving or deletion.
 * Constants: MAX_FILE_SIZE_BYTES = 5MB, MAX_FILES = 10, ALLOWED_CONTENT_TYPES = image/jpeg, image/png, image/webp.
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
        validateFiles(files);
        List<ImageRecord> records = buildImageRecords(itemId, files);
        List<ItemImage> saved = persistImageRecords(item, records);
        writeFilesToDisk(records);
        return saved;
    }

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

    private Item getItemOrThrow(int itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new Ecse321Project03Exception(HttpStatus.NOT_FOUND, "No item found with id " + itemId));
    }

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

    private List<ItemImage> persistImageRecords(Item item, List<ImageRecord> records) {
        List<ItemImage> saved = new ArrayList<>();
        for (ImageRecord r : records) {
            ItemImage im = new ItemImage(r.key, item);
            saved.add(itemImageRepository.save(im));
        }
        return saved;
    }

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
            deletePathsQuietly(written);
            throw new Ecse321Project03Exception(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to write image to disk: " + e.getMessage());
        }
    }

    private static void deletePathsQuietly(List<Path> paths) {
        for (Path p : paths) {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
        }
    }

    private static String extensionFromContentType(String contentType) {
        if (contentType == null) return "jpg";
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
     */
    public static Path pathFromKey(String key) {
        return Path.of(UPLOADS_BASE, key);
    }

    private static class ImageRecord {
        final String key;
        final byte[] bytes;

        ImageRecord(String key, byte[] bytes) {
            this.key = key;
            this.bytes = bytes;
        }
    }
}
