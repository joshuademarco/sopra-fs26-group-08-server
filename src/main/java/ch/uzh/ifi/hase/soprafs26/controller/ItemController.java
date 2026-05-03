package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.Item;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CharacterGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EquipItemsDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ItemGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ItemService;

@RestController
public class ItemController {

    private final ItemService itemService;

    ItemController(ItemService itemService) {
        this.itemService = itemService;

    }

    @GetMapping("/items")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ItemGetDTO> getAllItems() {
        List<ItemGetDTO> dtos = new ArrayList<>();
        for (Item item : itemService.getAllItems()) {
            dtos.add(DTOMapper.INSTANCE.convertEntityToItemGetDTO(item));
        }
        return dtos;
    }

    @GetMapping("/users/{userId}/items")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ItemGetDTO> getUserInventory(@PathVariable Long userId) {
        List<ItemGetDTO> dtos = new ArrayList<>();
        for (Item item : itemService.getUserInventory(userId)) {
            dtos.add(DTOMapper.INSTANCE.convertEntityToItemGetDTO(item));
        }
        return dtos;
    }

    @PostMapping("/users/{userId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grantItem(@PathVariable Long userId, @PathVariable Long itemId) {
        itemService.grantItem(userId, itemId);
    }

    @PutMapping("/characters/{characterId}/equipment")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CharacterGetDTO equipItems(@PathVariable Long characterId, @RequestBody EquipItemsDTO dto) {
        Character character = itemService.equipItems(characterId, dto);
        return DTOMapper.INSTANCE.convertEntityToCharacterGetDTO(character);
    }

}
