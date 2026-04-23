package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CharacterGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.CharacterService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

@RestController
@RequestMapping("/users/{userId}/character")
public class CharacterController {

    private final CharacterService characterService;
    private final UserService userService;

    public CharacterController(CharacterService characterService, UserService userService) {
        this.characterService = characterService;
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CharacterGetDTO getCharacter(@PathVariable Long userId, @CookieValue(name = "token", required = true) String token) {

        User requestingUser = userService.getUserByToken(token);
        
        if (!requestingUser.getId().equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.FORBIDDEN, "You can only access your own character");
        }

        Character character = characterService.getCharacterByUserId(userId);
        return DTOMapper.INSTANCE.convertEntityToCharacterGetDTO(character);
    }
}
