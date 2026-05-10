package ch.uzh.ifi.hase.soprafs26.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.RaidService;

@RestController
public class RaidController {

    private final RaidService raidService;

    RaidController(RaidService raidService) {
        this.raidService = raidService;
    }

    @PostMapping("/groups/{groupId}/raids")
    @ResponseStatus(HttpStatus.CREATED)
    public RaidGetDTO createRaid(@PathVariable Long groupId, @RequestBody RaidPostDTO dto) {
        BossRaid raid = raidService.createRaid(groupId, dto);
        return DTOMapper.INSTANCE.convertEntityToRaidGetDTO(raid);
    }

    @GetMapping("/groups/{groupId}/raids")
    public List<RaidGetDTO> getRaids(@PathVariable Long groupId) {
        List<BossRaid> raids = raidService.getRaidsByGroup(groupId);
        List<RaidGetDTO> dtos = new ArrayList<>();
        for (BossRaid raid : raids) {
            dtos.add(raidService.convertEntityToRaidGetDTO(raid));
        }
        return dtos;
    }

    @GetMapping("/raids/{raidId}")
    public RaidGetDTO getRaid(@PathVariable Long raidId) {
        BossRaid raid = raidService.getRaid(raidId);
        return raidService.convertEntityToRaidGetDTO(raid);
    }

    @PostMapping("/raids/{raidId}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public void joinRaid(@PathVariable Long raidId, @CookieValue(name = "token", required = true) String token) {
        raidService.joinRaid(raidId, token);
    }

    @PostMapping("/raids/{raidId}/rsvp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rsvpRaid(@PathVariable Long raidId, @RequestParam boolean accepted,
            @CookieValue(name = "token", required = true) String token) {
        raidService.rsvpRaid(raidId, accepted, token);
    }

    @PostMapping("/raids/{raidId}/tasks/{taskId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeTask(@PathVariable Long raidId, @PathVariable Long taskId, @RequestParam Boolean success,
            @CookieValue(name = "token", required = true) String token) {
        raidService.completeTask(raidId, taskId, success, token);
    }

    @PostMapping("/groups/{groupId}/raids/quick")
    @ResponseStatus(HttpStatus.CREATED)
    public RaidGetDTO quickStartRaid(@PathVariable Long groupId) {
        BossRaid raid = raidService.quickStartRaid(groupId);
        return raidService.convertEntityToRaidGetDTO(raid);
    }

    @PostMapping("/raids/{raidId}/reschedule")
    public RaidGetDTO rescheduleRaid(@PathVariable Long raidId,
            @RequestParam(defaultValue = "7") int windowDays) {
        BossRaid raid = raidService.rescheduleRaid(raidId, windowDays);
        return DTOMapper.INSTANCE.convertEntityToRaidGetDTO(raid);
    }

    // TODO: Remove this admin endpoint again

    @PostMapping("/admin/groups/{groupId}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public void adminScheduleGroup(@PathVariable Long groupId, @RequestParam(required = false) String earliest) {
        if (earliest != null && !earliest.isEmpty()) {
            Instant earliestTime = Instant.parse(earliest);
            raidService.adminScheduleForGroupWithEarliest(groupId, earliestTime);
        } else {
            raidService.adminAutoScheduleForGroup(groupId);
        }
    }

    @PostMapping("/admin/groups/{groupId}/start-raid")
    @ResponseStatus(HttpStatus.CREATED)
    public RaidGetDTO adminStartRaidImmediately(@PathVariable Long groupId) {
        BossRaid raid = raidService.adminStartRaidImmediately(groupId);
        return raidService.convertEntityToRaidGetDTO(raid);
    }

    @PostMapping("/admin/raids/{raidId}/fast-forward")
    public RaidGetDTO adminFastForward(@PathVariable Long raidId,
            @RequestParam(defaultValue = "10") int seconds) {
        BossRaid raid = raidService.adminFastForwardRaid(raidId, seconds);
        return raidService.convertEntityToRaidGetDTO(raid);
    }

    @PostMapping("/admin/raids/{raidId}/force-complete")
    public RaidGetDTO adminForceComplete(@PathVariable Long raidId,
            @RequestParam(defaultValue = "DEFEATED") String outcome) {
        BossRaid raid = raidService.adminForceCompleteRaid(raidId, outcome);
        return raidService.convertEntityToRaidGetDTO(raid);
    }

    @DeleteMapping("/admin/groups/{groupId}/raids")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adminClearRaids(@PathVariable Long groupId) {
        raidService.adminClearGroupRaids(groupId);
    }
}
