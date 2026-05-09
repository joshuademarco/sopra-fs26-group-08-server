package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.ChestPiece;
import ch.uzh.ifi.hase.soprafs26.entity.HandHeld;
import ch.uzh.ifi.hase.soprafs26.entity.Hat;
import ch.uzh.ifi.hase.soprafs26.entity.Item;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ItemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EquipItemsDTO;

public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CharacterRepository characterRepository;
    @InjectMocks
    private ItemService itemService;

    private User user;
    private Character character;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setToken("token");
        character = new Character();
        character.setHealth(100);
        character.setMaxHealth(100);
        user.setCharacter(character);
        character.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
    }

    @Test
    public void grantItem_validInputs_success() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Hat");
        Long itemId = 1L;

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        itemService.grantItem(1L, itemId);

        verify(userRepository).save(any());
    }

    @Test
    public void grantItem_itemAlreadyOwned_throwsConflict() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Hat");
        Long itemId = 1L;
        user.addItem(item);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.grantItem(1L, itemId));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

    }

    @Test
    public void grantItem_userNotFound_throwsNotFound() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Hat");
        Long itemId = 1L;

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.grantItem(2L, itemId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void grantItem_itemNotFound_throwsNotFound() {
        Long itemId = 1L;

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.grantItem(1L, itemId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void equipItems_validInputs_success() {
        Hat hat = new Hat();
        ChestPiece chest = new ChestPiece();
        HandHeld axe = new HandHeld();
        hat.setId(1L);
        hat.setName("Test Hat");
        chest.setId(2L);
        chest.setName("Test Chest");
        axe.setId(3L);
        axe.setName("Test Axe");
        user.addItem(axe);
        user.addItem(hat);
        user.addItem(chest);
        character.setUser(user);
        EquipItemsDTO dto = new EquipItemsDTO();
        dto.setHatId(1L);
        dto.setChestPieceId(2L);
        dto.setHandHeldId(3L);

        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(characterRepository.save(any())).thenReturn(character);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(hat));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(chest));
        when(itemRepository.findById(3L)).thenReturn(Optional.of(axe));

        Character result = itemService.equipItems(1L, dto);

        assertNotNull(result);
    }

    @Test
    public void equipItems_characterNotFound_throwsNotFound() {
        EquipItemsDTO dto = new EquipItemsDTO();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.equipItems(99L, dto));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void equipItems_itemNotInInventory_throwsForbidden() {
        Hat hat = new Hat();
        ChestPiece chest = new ChestPiece();
        hat.setId(1L);
        hat.setName("Test Hat");
        chest.setId(2L);
        chest.setName("Test Chest");
        user.addItem(hat);
        character.setUser(user);
        EquipItemsDTO dto = new EquipItemsDTO();
        dto.setHatId(1L);
        dto.setChestPieceId(2L);

        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(hat));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(chest));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.equipItems(1L, dto));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void equipItems_wrongForTypeHatSlot_throwsBadRequest() {
        ChestPiece chest = new ChestPiece();
        chest.setId(2L);
        chest.setName("Test Chest");
        user.addItem(chest);
        character.setUser(user);
        EquipItemsDTO dto = new EquipItemsDTO();
        dto.setHatId(2L);

        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(chest));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.equipItems(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void equipItems_wrongTypeForChestSlot_throwsBadRequest() {
        Hat hat = new Hat();
        hat.setId(1L);
        user.addItem(hat);
        character.setUser(user);
        EquipItemsDTO dto = new EquipItemsDTO();
        dto.setChestPieceId(1L);

        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(hat));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.equipItems(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void equipItems_wrongTypeForHandheldSlot_throwsBadRequest() {
        Hat hat = new Hat();
        hat.setId(1L);
        user.addItem(hat);
        character.setUser(user);
        EquipItemsDTO dto = new EquipItemsDTO();
        dto.setHandHeldId(1L);

        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(hat));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.equipItems(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void equipItems_nullSlotIds_unequipsCharacter() {
        character.setUser(user);
        EquipItemsDTO dto = new EquipItemsDTO();
        
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(characterRepository.save(any())).thenReturn(character);

        Character result = itemService.equipItems(1L, dto);

        assertNull(result.getEquippedHat());
        assertNull(result.getEquippedChestPiece());
        assertNull(result.getEquippedHandheld());
    }

    @Test
    public void getAllItems_returnsAllItems() {
        Item item1 = new Item();
        Item item2 = new Item();
        item1.setId(1L);
        item2.setId(2L);

        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));

        List<Item> items = itemService.getAllItems();

        assertEquals(2, items.size());
    }

    @Test
    public void getUserInventory_validUser_returnsInventory() {
        Item item1 = new Item();
        Item item2 = new Item();
        item1.setId(1L);
        item2.setId(2L);
        user.addItem(item1);
        user.addItem(item2);

        Set<Item> items = itemService.getUserInventory(1L);

        assertEquals(2, items.size());
    }

    @Test
    public void getUserInventory_userNotFound_throwsNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> itemService.getUserInventory(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
