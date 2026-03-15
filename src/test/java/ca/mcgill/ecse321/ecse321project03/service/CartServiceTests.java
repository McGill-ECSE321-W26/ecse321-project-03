package ca.mcgill.ecse321.ecse321project03.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ca.mcgill.ecse321.ecse321project03.dto.CartCreationDto;
import ca.mcgill.ecse321.ecse321project03.dto.CartItemDto;
import ca.mcgill.ecse321.ecse321project03.dto.ItemTypeDto;
import ca.mcgill.ecse321.ecse321project03.exception.Ecse321Project03Exception;
import ca.mcgill.ecse321.ecse321project03.model.*;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartItemRepository;
import ca.mcgill.ecse321.ecse321project03.repository.CartRepository;
import ca.mcgill.ecse321.ecse321project03.repository.ItemTypeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)  // replaces @SpringBootTest
public class CartServiceTests {
    @Mock
    private AccountRepository accountRepo;
    @Mock
    private CartRepository cartRepo;
    @Mock
    private CartItemRepository cartItemRepo;
    @Mock
    private ItemTypeRepository itemTypeRepo;
    @Mock
    private ItemType itemTypeMock;

    @InjectMocks
    private CartService cartService;

    private String username;
    private String email;
    private String password;
    private String address;
    private int loyaltyPts;
    private int id;
    private Account account;
    private Cart cart;
    private CartCreationDto cartDto;
    private CartItemDto cartItemDto;
    private CartItem cartItem;
    private CartItem.CartItemId cartItemId;
    private ItemType itemType;
    private ItemTypeDto itemTypeDto;
    private Item item;

    @BeforeEach
    void setup() {
        username = "user1";
        email = "email1@mail.com";
        password = "p";
        address = "123 Street";
        loyaltyPts = 0;
        id = 5;


        account = new Account(username, email, password, address, loyaltyPts);
        cart = new Cart(account);
        cartDto = new CartCreationDto(account);

        item = new Item("Shirt", Item.Tag.Top, 15);
        itemType = new ItemType(ItemType.Size.M, ItemType.Color.White, 2, item);
        itemTypeDto = new ItemTypeDto(itemType);
        cartItemId = new CartItem.CartItemId(itemType, cart);
        cartItem = new CartItem(cartItemId, 1);
        cartItemDto = new CartItemDto(cartItem);
    }

    @Test
    public void testCreateNewValidCart() {
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.save(any(Cart.class))).thenReturn(cart);

        Cart createdCart = cartService.createNewCart(id, cartDto);

        assertNotNull(createdCart);
        assertEquals(createdCart.getAccount(), account);
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    public void testCreateNewCartNullCustomer() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.createNewCart(id, cartDto));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No person with id " + id + " found.", e.getMessage());
    }

    @Test
    public void testCreateNewCartCartExists() {
        Cart existantCart = new Cart(account);
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(existantCart);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.createNewCart(id, cartDto));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("A cart already exists! Users may only have one active cart at the time.", e.getMessage());
    }

    @Test
    public void testDeleteValidCartWithItems(){
        CartItem.CartItemId cartItemid = new CartItem.CartItemId(itemTypeMock, cart);
        CartItem cartItem = new CartItem(cartItemid, 2);
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);
        when(cartItemRepo.findByCartItemId_Cart(cart)).thenReturn(List.of(cartItem));

        assertDoesNotThrow(() -> cartService.deleteCart(id));

        verify(cartItemRepo, times(1)).deleteAll(List.of(cartItem));
        verify(cartRepo, times(1)).delete(cart);
    }

    @Test
    public void testDeleteValidCartWithoutItems(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);

        assertDoesNotThrow(() -> cartService.deleteCart(id));

        verify(cartItemRepo, times(0)).deleteAll(any());
        verify(cartRepo, times(1)).delete(cart);
    }

    @Test
    public void testDeleteCartInvalidId() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.deleteCart(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No person with id " + id + " found.", e.getMessage());
    }

    @Test
    public void testDeleteCartNoCart(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.deleteCart(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("Impossible to delete the cart. No carts are associate to this customer.", e.getMessage());
    }


    @Test
    public void testAddItemToCartInvalidId() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.addItemToCart(id, cartItemDto));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("No account with id " + id + " found.", e.getMessage());
    }

    @Test
    public void testAddItemToCartNoCart(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.addItemToCart(id, cartItemDto));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("Cannot add an item to the cart. This customer doesn't have a cart!", e.getMessage());
    }

    @Test
    public void testAddNullItemToCartItem() {
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.addItemToCart(id, null));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Cart item data must not be null.", e.getMessage());
    }

    @Test
    public void testAddNullItemTypeItemToCart() {
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);

        CartItemDto cartItemDtoMock = mock(CartItemDto.class);
        when(cartItemDtoMock.getItemType()).thenReturn(null);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.addItemToCart(id, cartItemDtoMock));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Item type must not be null.", e.getMessage());
    }

    @Test
    public void testAddInvalidQtityItemtoCart(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);

        CartItemDto cartItemDtoMock = mock(CartItemDto.class);
        when(cartItemDtoMock.getItemType()).thenReturn(itemTypeDto);
        when(cartItemDtoMock.getQuantity()).thenReturn(0);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.addItemToCart(id, cartItemDtoMock));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Quantity must be positive.", e.getMessage());
    }

    @Test
    public void testAddItemToCartItemTypeNotFound(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);

        ItemTypeDto itemTypeDtoMock = mock(ItemTypeDto.class);
        when(itemTypeDtoMock.getId()).thenReturn(1);

        CartItemDto cartItemDtoMock = mock(CartItemDto.class);
        when(cartItemDtoMock.getItemType()).thenReturn(itemTypeDtoMock);
        when(cartItemDtoMock.getQuantity()).thenReturn(2);

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.addItemToCart(id, cartItemDtoMock));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "No item type found with id " + 1 + ".", e.getMessage());
    }

    @Test
    public void testAddItemToCartQtityGreaterThanStock(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);

        //itemType stock is 2 as defined in beforeEach method
        cartItem = new CartItem(cartItemId, 5);
        cartItemDto = new CartItemDto(cartItem);

        when(itemTypeRepo.findById(itemTypeDto.getId())).thenReturn(Optional.of(itemType));

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.addItemToCart(id, cartItemDto));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals( "Requested quantity exceeds available stock.", e.getMessage());

    }

    @Test
    public void testIncreaseQuantityExistingItemToCart() {
        CartItem existingCartItem = new CartItem(cartItemId, 3);

        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);
        when(itemTypeRepo.findById(itemTypeDto.getId())).thenReturn(Optional.of(itemType));
        when(cartItemRepo.findById(cartItemId)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepo.save(any(CartItem.class))).thenReturn(existingCartItem);

        CartItem returnedCartItem = cartService.addItemToCart(id, cartItemDto);

        assertNotNull(returnedCartItem);
        assertEquals( existingCartItem.getCartItemId(), returnedCartItem.getCartItemId());
        assertEquals(4, returnedCartItem.getQuantity());
        verify(cartItemRepo, times(1)).save(any(CartItem.class));
    }

    @Test
    public void testAddNewItemToCart(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);
        when(itemTypeRepo.findById(itemTypeDto.getId())).thenReturn(Optional.of(itemType));
        when(cartItemRepo.save(any(CartItem.class))).thenReturn(cartItem);

        CartItem returnedCartItem = cartService.addItemToCart(id, cartItemDto);

        assertNotNull(returnedCartItem);
        assertEquals(cartItem.getCartItemId(), returnedCartItem.getCartItemId());
        assertEquals(1, returnedCartItem.getQuantity());
        verify(cartItemRepo, times(1)).save(any(CartItem.class));
    }

    @Test
    public void testFindCartTotalPriceValidId(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));
        when(cartRepo.findByAccount_Id(id)).thenReturn(cart);
        when(cartItemRepo.findByCartItemId_Cart(cart)).thenReturn(List.of(cartItem));

        Double returnedPrice = cartService.findCartTotalPrice(id);

        assertNotNull(returnedPrice);
        assertEquals(15, returnedPrice);
    }

    @Test
    public void testFindCartTotalPriceInvalidId() {
        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.findCartTotalPrice(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "No person with id " + id + " found.", e.getMessage());
    }

    @Test
    public void testFindCartTotalPriceCartNotFound(){
        when(accountRepo.findById(id)).thenReturn(Optional.of(account));

        Ecse321Project03Exception e =
                assertThrows(Ecse321Project03Exception.class,
                        () -> cartService.findCartTotalPrice(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals( "This user's cart does not exist. Cannot calculate the cart total.", e.getMessage());
    }
}
