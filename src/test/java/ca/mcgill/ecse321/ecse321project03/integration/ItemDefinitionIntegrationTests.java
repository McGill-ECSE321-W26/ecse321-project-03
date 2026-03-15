package ca.mcgill.ecse321.ecse321project03.integration;
import org.aspectj.lang.annotation.Before;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import ca.mcgill.ecse321.ecse321project03.dto.ErrorDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemDefinitionResponseDto;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.repository.ItemImageRepository;
import org.springframework.util.LinkedMultiValueMap;
import java.math.BigDecimal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
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
    
    @Autowired
    private AccountRepository accountRepository;

    private int accountId;

    @BeforeAll
    @AfterAll
    public void cleanup() {
        itemImageRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        itemImageRepository.deleteAll();
        accountRepository.deleteAll();

        Account account = new Account("Test", "manager@test.com", "Password123!", "123 Street", 1);
        accountRepository.save(account);
        accountId = account.getId();
    }

    
    /*
    Valid test for endpoint POST create item
    @author: Maria Klinski
     */
    @Test
    @Order(0)
    public void testCreateItemValid() {
        
        int accountId = 1;
        ItemCreationDto itemCreationDto = new ItemCreationDto();
        itemCreationDto.setName("Test Item");
        itemCreationDto.setTag("Test Tag");
        itemCreationDto.setPrice(new BigDecimal("9.99"));
        
        // MultipartFile
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accountId", accountId);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ItemCreationDto> item = new HttpEntity<>(itemCreationDto, jsonHeaders);
        body.add("item", item);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);


        // Act
        ResponseEntity<ItemDefinitionResponseDto> response = client.postForEntity("/items", request, ItemDefinitionResponseDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue("Test Item".equals(response.getBody().getName()));
    }

    /*
    Invalid test for endpoint POST create item, missing item
    @author: Maria Klinski
     */
    @Test
    @Order(1)
    public void testCreateItemInvalidMissingItem() {
        
        // MultipartFile
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accountId", 1);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ErrorDto> response = client.postForEntity("/items", request, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    
}
