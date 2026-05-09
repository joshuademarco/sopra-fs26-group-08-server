package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.Item;
import ch.uzh.ifi.hase.soprafs26.service.ItemService;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    private Character character;
    private Item item;

    @BeforeEach
    public void setup() {
        character = new Character();
        character.setId(1L);
        item = new Item();
        item.setId(1L);
        item.setName("Wizard Hat");
        item.setAssetKey("wizard_1");
        item.setItemType("HAT");
    }

    @Test
    public void getAllItems_returnsListOfItems() throws Exception {
        given(itemService.getAllItems()).willReturn(List.of(item));

        mockMvc.perform(get("/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Wizard Hat")));

    }

    @Test
    public void getUserInventory_returnsUserInventory() throws Exception {
        given(itemService.getUserInventory(1L)).willReturn(Set.of(item));

        mockMvc.perform(get("/users/1/items")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Wizard Hat")));

    }

    @Test
    public void grantItem_returnsNoContent() throws Exception {
        mockMvc.perform(post("/users/1/items/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void equipItems_returnsCharacter() throws Exception {
        given(itemService.equipItems(any(), any())).willReturn(character);

        mockMvc.perform(put("/characters/1/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hatId\": 1, \"chestPieceId\": 2, \"handHeldId\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
}
