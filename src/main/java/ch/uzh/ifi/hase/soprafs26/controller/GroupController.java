package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupJoinDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GroupService;

@RestController
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    public List<GroupGetDTO> getGroups(@CookieValue(name = "token", required = true) String tokenCookie) {
        List<Group> groups = groupService.getMyGroups(tokenCookie);
        List<GroupGetDTO> dtos = new ArrayList<>();
    
        for (Group g : groups) {
            dtos.add(DTOMapper.INSTANCE.convertEntityToGroupGetDTO(g));
        }
        return dtos;
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupGetDTO createGroup(@RequestBody GroupPostDTO dto, @CookieValue(name = "token", required = true) String tokenCookie) {
        Group input = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(dto);
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(groupService.createGroup(input, tokenCookie));
    }

    @PostMapping("/groups/join") 
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinGroup(@RequestBody GroupJoinDTO dto, @CookieValue(name = "token", required = true) String tokenCookie) {
        groupService.joinGroup(dto.getName(), dto.getPassword(), tokenCookie);
    }
}