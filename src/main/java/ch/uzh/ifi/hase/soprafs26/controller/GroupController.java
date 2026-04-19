package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GroupService;

@RestController
public class GroupController {

  private final GroupService groupService;

  public GroupController(GroupService groupService) {
    this.groupService = groupService;
  }

  @PostMapping("/groups")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public GroupGetDTO createGroup(@RequestBody GroupPostDTO groupPostDTO) {
    Group groupInput = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(groupPostDTO);

    Group createdGroup = groupService.createGroup(groupInput);

    return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(createdGroup);
  }

  @GetMapping("/groups")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<GroupGetDTO> getAllGroups() {
    List<Group> groups = groupService.getAllGroups();
    List<GroupGetDTO> groupGetDTOs = new ArrayList<>();

    for (Group group : groups) {
      groupGetDTOs.add(DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group));
    }
    
    return groupGetDTOs;
  }
}