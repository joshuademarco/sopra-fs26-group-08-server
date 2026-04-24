package ch.uzh.ifi.hase.soprafs26.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RaidPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.RaidService;
import jakarta.servlet.http.Cookie;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(RaidController.class)
public class RaidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RaidService raidService;

    @Test
    public void createRaid_validInput_returns201WithRaidData() throws Exception {
        BossRaid raid = new BossRaid();
        raid.setId(1L);
        raid.setName("Dragon");
        raid.setHealth(500);
        raid.setMaxHealth(500);
        raid.setDurationSeconds(3600);
        raid.setStatus(RaidStatus.SCHEDULED);

        given(raidService.createRaid(eq(10L), any(RaidPostDTO.class))).willReturn(raid);

        RaidPostDTO dto = new RaidPostDTO();
        dto.setName("Dragon");
        dto.setDurationSeconds(3600);
        dto.setHealth(500);

        mockMvc.perform(post("/groups/10/raids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Dragon")))
                .andExpect(jsonPath("$.health", is(500)));
    }

    @Test
    public void getRaids_returnsListOfRaids() throws Exception {
        BossRaid raid = new BossRaid();
        raid.setId(1L);

        RaidGetDTO dto = new RaidGetDTO();
        dto.setId(1L);
        dto.setName("Dragon");

        given(raidService.getRaidsByGroup(10L)).willReturn(List.of(raid));
        given(raidService.convertEntityToRaidGetDTO(raid)).willReturn(dto);

        mockMvc.perform(get("/groups/10/raids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Dragon")));
    }

    @Test
    public void getRaid_returnsRaid() throws Exception {
        BossRaid raid = new BossRaid();
        raid.setId(1L);

        RaidGetDTO dto = new RaidGetDTO();
        dto.setId(1L);
        dto.setName("Dragon");
        dto.setStatus(RaidStatus.ACTIVE);

        given(raidService.getRaid(1L)).willReturn(raid);
        given(raidService.convertEntityToRaidGetDTO(raid)).willReturn(dto);

        mockMvc.perform(get("/raids/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    public void joinRaid_withToken_returns201() throws Exception {
        mockMvc.perform(post("/raids/1/join")
                .cookie(new Cookie("token", "my-token")))
                .andExpect(status().isCreated());

        verify(raidService).joinRaid(1L, "my-token");
    }

    @Test
    public void completeTask_withSuccessTrue_returns204() throws Exception {
        mockMvc.perform(post("/raids/1/tasks/5/complete")
                .cookie(new Cookie("token", "my-token"))
                .param("success", "true"))
                .andExpect(status().isNoContent());

        verify(raidService).completeTask(1L, 5L, true, "my-token");
    }

    @Test
    public void completeTask_raidNotActive_returns409() throws Exception {
        given(raidService.completeTask(any(), any(), any(), any()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Raid is not active"));

        mockMvc.perform(post("/raids/1/tasks/5/complete")
                .cookie(new Cookie("token", "my-token"))
                .param("success", "true"))
                .andExpect(status().isConflict());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
