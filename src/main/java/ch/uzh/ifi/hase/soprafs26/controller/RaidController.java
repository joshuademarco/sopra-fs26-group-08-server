package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.List;
import java.util.stream.Collectors;

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
        return raidService.getRaidsByGroup(groupId).stream().map(DTOMapper.INSTANCE::convertEntityToRaidGetDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/raids/{raidId}/reschedule")
    public RaidGetDTO rescheduleRaid(@PathVariable Long raidId, @RequestParam(defaultValue = "7") int windowDays) {
        BossRaid raid = raidService.rescheduleRaid(raidId, windowDays);
        return DTOMapper.INSTANCE.convertEntityToRaidGetDTO(raid);
    }
}
