package ca.mcgill.ecse321.ecse321project03.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.ItemImage;
import ca.mcgill.ecse321.ecse321project03.model.Item.Tag;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemImageRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ManagerRepository;

@SpringBootTest
public class ImageServiceTests {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemImageRepository itemImageRepository;

    @InjectMocks
    private ImageService service;

    private static final int MANAGER_ACCOUNT_ID = 1;
    private static final int ITEM_ID = 10;
    private Account managerAccount;
    private Manager manager;
    private Item item;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        managerAccount = new Account("managerUser", "manager@mail.com", "pass", "addr", 0);
        managerAccount.setId(MANAGER_ACCOUNT_ID);
        manager = new Manager(managerAccount);
        item = new Item("Test Item", Tag.Top, 19.99);
        item.setId(ITEM_ID);
        // creates the folder "uploads/" if it does not exist already
        Files.createDirectories(Path.of("uploads"));
    }

    // ---------- uploadItemImages: success ----------
    @Test
    public void testUploadItemImagesSuccess() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        // use MockMultipartFile to create a fake file
        MockMultipartFile file = new MockMultipartFile("img", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
        List<MultipartFile> files = List.of(file); // make it a list to use as input to service method

        when(itemImageRepository.save(any(ItemImage.class))).thenAnswer(inv -> {
            ItemImage im = inv.getArgument(0);
            im.setId(1);
            return im;
        });

        List<ItemImage> result = service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        // verify the value of key except for UUID part which is random
        // verify beginning of key is "items/10/"
        assertEquals("items/10/", result.get(0).getKey().substring(0, 9));
        // verify ending of key is ".jpg"
        assertEquals(".jpg", result.get(0).getKey().substring(result.get(0).getKey().length() - 4));
        // verify item is set to the item passed in
        assertEquals(item, result.get(0).getItem());
        // verify repository methods are called
        verify(accountRepository, times(1)).findById(MANAGER_ACCOUNT_ID);
        verify(managerRepository, times(1)).findByAccount_Id(MANAGER_ACCOUNT_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(itemImageRepository, times(1)).save(any(ItemImage.class));
    }

    // ---------- uploadItemImages: ensureManager failures ----------
    @Test
    public void testUploadItemImagesUserNotLoggedIn() {
        when(accountRepository.findById(anyInt())).thenReturn(Optional.empty());
        List<MultipartFile> files = List.of(new MockMultipartFile("f", "f.jpg", "image/jpeg", new byte[]{1}));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(999, ITEM_ID, files));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        assertEquals("User not logged in.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesNotManager() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(null);
        List<MultipartFile> files = List.of(new MockMultipartFile("f", "f.jpg", "image/jpeg", new byte[]{1}));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("Only managers can upload item images.", e.getMessage());
    }

    // ---------- uploadItemImages: getItemOrThrow failure ----------
    @Test
    public void testUploadItemImagesItemNotFound() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
        List<MultipartFile> files = List.of(new MockMultipartFile("f", "f.jpg", "image/jpeg", new byte[]{1}));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No item found with id " + ITEM_ID, e.getMessage());
    }

    // ---------- uploadItemImages: validateFiles failures ----------
    @Test
    public void testUploadItemImagesFilesNull() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, null));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("At least one image file is required.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesFilesEmpty() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, new ArrayList<>()));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("At least one image file is required.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesTooManyFiles() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        // The maximum allowed number of images is 10, we create 11 deliberately
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            files.add(new MockMultipartFile("f" + i, "f.jpg", "image/jpeg", new byte[]{1}));
        }

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Maximum 10 files allowed, got 11.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesFileNullAtSlot() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        List<MultipartFile> files = new ArrayList<>();
        files.add(new MockMultipartFile("f0", "f0.jpg", "image/jpeg", new byte[]{1}));
        files.add(null); // attach a null file

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("files[1]: file must not be empty.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesFileEmptyAtSlot() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        MockMultipartFile emptyFile = new MockMultipartFile("empty", "empty.jpg", "image/jpeg", new byte[0]);
        List<MultipartFile> files = List.of(emptyFile); // attach empty file

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("files[0]: file must not be empty.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesInvalidContentType() {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        // create a fake file using mock object
        // pretend there is a file of in pdf format, which is not allowed
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(100L); //100 bytes
        List<MultipartFile> files = List.of(file);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("files[0]: content type must be one of image/jpeg, image/png, image/webp.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesFileSizeExceedsMax() throws IOException {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        // create a fake file using mock object
        // Set it to 5 megabytes + 1, this exceeds the maximum file size
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(5L * 1024 * 1024 + 1);
        List<MultipartFile> files = List.of(file);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("files[0]: size must not exceed 5MB.", e.getMessage());
    }

    @Test
    public void testUploadItemImagesGetBytesThrows() throws IOException {
        when(accountRepository.findById(MANAGER_ACCOUNT_ID)).thenReturn(Optional.of(managerAccount));
        when(managerRepository.findByAccount_Id(MANAGER_ACCOUNT_ID)).thenReturn(manager);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        // create a fake file using mock object
        // This time the read will fail
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(100L);
        when(file.getBytes()).thenThrow(new IOException("read failed"));
        List<MultipartFile> files = List.of(file);

        Ecse321Project03Exception e = assertThrows(Ecse321Project03Exception.class,
                () -> service.uploadItemImages(MANAGER_ACCOUNT_ID, ITEM_ID, files));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Failed to read file: read failed", e.getMessage());
    }
}
