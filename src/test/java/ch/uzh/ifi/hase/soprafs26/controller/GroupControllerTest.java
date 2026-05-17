package ch.uzh.ifi.hase.soprafs26.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.HabitRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupJoinDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPutDTO;
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

        @MockitoBean
        private HabitRepository habitRepository;

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

        @Test
        public void getGroups_withPositiveHabits_returnsCorrectCounts() throws Exception {
                User member = new User();
                member.setId(10L);
                member.setUsername("alice");
                member.setStatus(UserStatus.ONLINE);

                Group group = new Group();
                group.setId(1L);
                group.setName("Test Group");
                group.setCreatedBy("creator");
                group.setCreatedAt(LocalDateTime.now());
                group.getUsers().add(member);

                Habit completedHabit = new Habit();
                completedHabit.setId(1L);
                completedHabit.setTitle("Run");
                completedHabit.setCategory(HabitCategory.PHYSICAL);
                completedHabit.setFrequency(HabitFrequency.DAILY);
                completedHabit.setPositive(true);
                completedHabit.setCompleted(true);
                completedHabit.setStreak(1);

                Habit incompleteHabit = new Habit();
                incompleteHabit.setId(2L);
                incompleteHabit.setTitle("Meditate");
                incompleteHabit.setCategory(HabitCategory.EMOTIONAL);
                incompleteHabit.setFrequency(HabitFrequency.DAILY);
                incompleteHabit.setPositive(true);
                incompleteHabit.setCompleted(false);
                incompleteHabit.setStreak(0);

                given(groupService.getMyGroups("token")).willReturn(Collections.singletonList(group));
                given(habitRepository.findByUserId(10L)).willReturn(List.of(completedHabit, incompleteHabit));

                mockMvc.perform(get("/groups")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].users[0].completedHabits", is(1)))
                                .andExpect(jsonPath("$[0].users[0].totalHabits", is(2)));
        }

        @Test
        public void leaveGroup_validInput_returnsNoContent() throws Exception {
                mockMvc.perform(delete("/groups/1/leave")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNoContent());

                verify(groupService).leaveGroup(1L, "token");
        }

        @Test
        public void leaveGroup_ownerCannotLeave_returnsForbidden() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner cannot leave."))
                                .when(groupService).leaveGroup(1L, "token");

                mockMvc.perform(delete("/groups/1/leave")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void leaveGroup_unauthorized_returnsUnauthorized() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired."))
                                .when(groupService).leaveGroup(1L, "token");

                mockMvc.perform(delete("/groups/1/leave")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        public void leaveGroup_groupNotFound_returnsNotFound() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."))
                                .when(groupService).leaveGroup(1L, "token");

                mockMvc.perform(delete("/groups/1/leave")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void updateGroup_validOwner_returnsUpdatedGroup() throws Exception {
                GroupPutDTO dto = new GroupPutDTO();
                dto.setName("New Name");

                Group updatedGroup = new Group();
                updatedGroup.setId(1L);
                updatedGroup.setName("New Name");
                updatedGroup.setCreatedBy("creator");
                updatedGroup.setCreatedAt(LocalDateTime.now());

                given(groupService.updateGroup(eq(1L), any(GroupPutDTO.class), eq("token"))).willReturn(updatedGroup);

                mockMvc.perform(put("/groups/1")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name", is("New Name")));
        }

        @Test
        public void updateGroup_notOwner_returnsForbidden() throws Exception {
                GroupPutDTO dto = new GroupPutDTO();
                dto.setName("New Name");

                given(groupService.updateGroup(eq(1L), any(GroupPutDTO.class), eq("token")))
                                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                "Only the owner can edit this group."));

                mockMvc.perform(put("/groups/1")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(dto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void updateGroup_groupNotFound_returnsNotFound() throws Exception {
                GroupPutDTO dto = new GroupPutDTO();
                dto.setName("New Name");

                given(groupService.updateGroup(eq(1L), any(GroupPutDTO.class), eq("token")))
                                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

                mockMvc.perform(put("/groups/1")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(dto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void deleteGroup_validOwner_returnsNoContent() throws Exception {
                mockMvc.perform(delete("/groups/1")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNoContent());

                verify(groupService).deleteGroup(1L, "token");
        }

        @Test
        public void deleteGroup_notOwner_returnsForbidden() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete this group."))
                                .when(groupService).deleteGroup(1L, "token");

                mockMvc.perform(delete("/groups/1")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void deleteGroup_groupNotFound_returnsNotFound() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."))
                                .when(groupService).deleteGroup(1L, "token");

                mockMvc.perform(delete("/groups/1")
                                .cookie(new Cookie("token", "token"))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
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
