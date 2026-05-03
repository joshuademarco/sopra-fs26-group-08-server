package ch.uzh.ifi.hase.soprafs26.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs26.entity.Item;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.ChestPiece;
import ch.uzh.ifi.hase.soprafs26.entity.HandHeld;
import ch.uzh.ifi.hase.soprafs26.entity.Hat;
import ch.uzh.ifi.hase.soprafs26.repository.ItemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.CharacterRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EquipItemsDTO;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CharacterRepository characterRepository;

    @Autowired
    public ItemService(@Qualifier("userRepository") UserRepository userRepository,
            @Qualifier("itemRepository") ItemRepository itemRepository,
            @Qualifier("characterRepository") CharacterRepository characterRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.characterRepository = characterRepository;

    }

    public void grantItem(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (user.hasItem(item)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already owns this item");
        }

        user.addItem(item);
        userRepository.save(user);
    }

    public Character equipItems(Long characterId, EquipItemsDTO dto) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));

        User user = character.getUser();

        if (dto.getHatId() != null) {
            Item hat = itemRepository.findById(dto.getHatId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
            if (!user.hasItem(hat)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Item not in inventory");
            }
            if (!(hat instanceof Hat)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not a hat");
            }
            character.setEquippedHat(hat);
        } else {
            character.setEquippedHat(null);
        }

        if (dto.getChestPieceId() != null) {
            Item chestPiece = itemRepository.findById(dto.getChestPieceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
            if (!user.hasItem(chestPiece)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Item not in inventory");
            }
            if (!(chestPiece instanceof ChestPiece)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not a chest piece");
            }
            character.setEquippedChestPiece(chestPiece);
        } else {
            character.setEquippedChestPiece(null);
        }

        if (dto.getHandHeldId() != null) {
            Item handHeld = itemRepository.findById(dto.getHandHeldId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
            if (!user.hasItem(handHeld)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Item not in inventory");
            }
            if (!(handHeld instanceof HandHeld)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not a hand held item");
            }
            character.setEquippedHandheld(handHeld);
        } else {
            character.setEquippedHandheld(null);
        }

        return characterRepository.save(character);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Set<Item> getUserInventory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getInventory();
    }
}
