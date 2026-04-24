package ch.uzh.ifi.hase.soprafs26.controller;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupJoinDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.GroupService;
import jakarta.servlet.http.Cookie;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(GroupController.class)
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @Test
    public void givenGroups_whenGetGroups_thenReturnJsonArray() throws Exception {
        Group group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setCreatedBy("creator");
        group.setCreatedAt(LocalDateTime.now());

        given(groupService.getMyGroups("token")).willReturn(Collections.singletonList(group));

        MockHttpServletRequestBuilder getRequest = get("/groups")
                .cookie(new Cookie("token", "token"))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Group")))
                .andExpect(jsonPath("$[0].createdBy", is("creator")));
    }

    @Test
    public void createGroup_validInput_returnsCreatedGroup() throws Exception {
        GroupPostDTO dto = new GroupPostDTO();
        dto.setName("Test Group");
        dto.setPassword("secret");

        Group createdGroup = new Group();
        createdGroup.setId(1L);
        createdGroup.setName("Test Group");
        createdGroup.setCreatedBy("creator");
        createdGroup.setCreatedAt(LocalDateTime.now());

        given(groupService.createGroup(any(Group.class), eq("token"))).willReturn(createdGroup);

        MockHttpServletRequestBuilder postRequest = post("/groups").cookie(new Cookie("token", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Group")))
                .andExpect(jsonPath("$.createdBy", is("creator")));
    }

    @Test
    public void joinGroup_validInput_returnsNoContent() throws Exception {
        GroupJoinDTO dto = new GroupJoinDTO();
        dto.setName("Test Group");
        dto.setPassword("secret");

        MockHttpServletRequestBuilder postRequest = post("/groups/join")
                .cookie(new Cookie("token", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto));

        mockMvc.perform(postRequest).andExpect(status().isNoContent());

        verify(groupService).joinGroup("Test Group", "secret", "token");
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
