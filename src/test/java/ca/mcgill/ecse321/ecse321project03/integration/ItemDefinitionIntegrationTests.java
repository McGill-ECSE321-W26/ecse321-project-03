package ca.mcgill.ecse321.ecse321project03.integration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import ca.mcgill.ecse321.ecse321project03.dto.ItemImageDto;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
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

public class ItemDefinitionIntegrationTests {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private ItemImageRepository itemImageRepository;
    

    @BeforeAll
    @AfterAll
    public void cleanup() {
        itemImageRepository.deleteAll();
    }
    // POST: test create item
    @Test
    @Order(0)
    public void testCreateItem() {
        
        // MultipartFile
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accountId", 1);

        

        

        // Act
        String url = "/images/items/1?accountId=1";
        ResponseEntity<ItemImageDto[]> response = client.postForEntity(url, body, ItemImageDto[].class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }


    
    
}
