package ca.mcgill.ecse321.ecse321project03.integration;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.AccountResponseDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemImageDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemImageRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// source: tutorial 7
// author: Maria Klinski

// embedded server will start at a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// tests will be run based on @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

// instance will be shared across all methods
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class ImageIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private ItemImageRepository itemImageRepository;

    @Autowired
    private AccountRepository accountRepository;
    

    @BeforeAll
    @AfterAll
    public void cleanup() {
        itemImageRepository.deleteAll();
    }
    // POST: upload item images
    @Test
    @Order(0)
    public void testUploadItemImages() {
        
        // MultipartFile
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", new ByteArrayResource("image".getBytes()) {
            @Override
            public String getFilename() {
                return "test_image.jpg";
            }
        });

        

        // Act
        String url = "/images/items/1?accountId=1";
        ResponseEntity<ItemImageDto[]> response = client.postForEntity(url, body, ItemImageDto[].class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    // POST: upload item images with invalid account
    @Test
    @Order(1)
    public void testUploadItemImagesInvalidAccount() {
        // MultipartFile
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", new ByteArrayResource("image".getBytes()) {
            @Override
            public String getFilename() {
                return "test_image.jpg";
            }
        });

        // Act
        String url = "/images/items/1?accountId=9999";
        ResponseEntity<ErrorDto> response = client.postForEntity(url, body, ErrorDto.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(List.of("No account found with id 9999"), response.getBody().getErrors());
    
}
}
