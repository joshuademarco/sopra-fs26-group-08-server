package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.repository.HabitRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupJoinDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GroupService;

@RestController
public class GroupController {

    private final GroupService groupService;
    private final HabitRepository habitRepository;

    public GroupController(GroupService groupService, HabitRepository habitRepository) {
        this.groupService = groupService;
        this.habitRepository = habitRepository;
    }

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    public List<GroupGetDTO> getGroups(@CookieValue(name = "token", required = true) String tokenCookie) {
        List<Group> groups = groupService.getMyGroups(tokenCookie);
        List<GroupGetDTO> dtos = new ArrayList<>();
        // for each group, we need total and completed positive habits for each member
        // -> for group habit progress in frontend
        for (Group g : groups) {
            GroupGetDTO dto = DTOMapper.INSTANCE.convertEntityToGroupGetDTO(g);
            for (GroupGetDTO.GroupMember member : dto.getUsers()) {
                List<Habit> allHabits = habitRepository.findByUserId(member.getId());
                List<Habit> habits = new ArrayList<>();
                for (Habit h : allHabits) {
                    if (Boolean.TRUE.equals(h.getPositive()))
                        habits.add(h);
                }
                int completed = 0;
                for (Habit h : habits) {
                    if (Boolean.TRUE.equals(h.getCompleted()))
                        completed++;
                }
                member.setTotalHabits(habits.size());
                member.setCompletedHabits(completed);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupGetDTO createGroup(@RequestBody GroupPostDTO dto,
            @CookieValue(name = "token", required = true) String tokenCookie) {
        Group input = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(dto);
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(groupService.createGroup(input, tokenCookie));
    }

    @PostMapping("/groups/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinGroup(@RequestBody GroupJoinDTO dto,
            @CookieValue(name = "token", required = true) String tokenCookie) {
        groupService.joinGroup(dto.getName(), dto.getPassword(), tokenCookie);
    }

    @DeleteMapping("/groups/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(@PathVariable Long id, @CookieValue(name = "token", required = true) String tokenCookie) {
        groupService.leaveGroup(id, tokenCookie);
    }

    @PutMapping("/groups/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupGetDTO updateGroup(@PathVariable Long id, @RequestBody GroupPutDTO dto,
            @CookieValue(name = "token", required = true) String tokenCookie) {
        Group updated = groupService.updateGroup(id, dto, tokenCookie);
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(updated);
    }

    @DeleteMapping("/groups/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long id, @CookieValue(name = "token", required = true) String tokenCookie) {
        groupService.deleteGroup(id, tokenCookie);
    }
}