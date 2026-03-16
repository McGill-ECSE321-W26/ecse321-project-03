package ca.mcgill.ecse321.ecse321project03.integration;
import ca.mcgill.ecse321.ecse321project03.model.Item;
import ca.mcgill.ecse321.ecse321project03.model.Manager;
import ca.mcgill.ecse321.ecse321project03.repository.RoleRepository;
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
import java.util.List;

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

    @Autowired
    private RoleRepository roleRepository;

    private int accountId;

    @BeforeAll
    @AfterAll
    public void cleanup() {

        itemImageRepository.deleteAll();
        roleRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {

        Account account = new Account("Test", "manager@test.com", "Password123!", "123 Street", 1);
        accountRepository.save(account);
        accountId = account.getId();

        Manager managerRole = new Manager(account);
        roleRepository.save(managerRole);
    }

    
    /*
    Valid test for endpoint POST create item
    @author: Maria Klinski
     */
    @Test
    @Order(0)
    public void testCreateItemValid() {
        
        ItemCreationDto itemCreationDto = new ItemCreationDto();
        itemCreationDto.setName("Test Item");
        itemCreationDto.setTag("Accessory");
        itemCreationDto.setPrice(new BigDecimal("9.99"));

        // Build the missing VariantSpec!
        ItemCreationDto.VariantSpec variant1 = new ItemCreationDto.VariantSpec();
        variant1.setColor("Black");
        variant1.setSize("M");
        variant1.setInitialStock(10);

        itemCreationDto.setVariants(List.of(variant1));

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

        // Act: Catch the raw String response instead of the DTO
        ResponseEntity<String> response = client.postForEntity("/items", request, String.class);

        // Print the exact error!
        System.out.println("SERVER RESPONSE: " + response.getBody());

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

//        // Act
//        ResponseEntity<ItemDefinitionResponseDto> response = client.postForEntity("/items", request, ItemDefinitionResponseDto.class);
//
//        // Assert
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertTrue("Test Item".equals(response.getBody().getName()));
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
