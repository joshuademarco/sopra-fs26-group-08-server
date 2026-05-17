package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.HabitCategory;
import ch.uzh.ifi.hase.soprafs26.constant.HabitFrequency;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.HabitGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TodoGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
	@Test
	public void testCreateUser_fromUserPostDTO_toUser_success() {
		// create UserPostDTO
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setEmail("testemail@uzh.ch");
		userPostDTO.setPassword("password123");
		userPostDTO.setUsername("testUsername");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getEmail(), user.getEmail());
		assertEquals(userPostDTO.getPassword(), user.getPassword());
		assertEquals(userPostDTO.getUsername(), user.getUsername());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setEmail("testemail@uzh.ch");
		user.setPassword("password123");
		user.setUsername("testUsername");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getEmail(), userGetDTO.getEmail());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
	}

	@Test
	public void testCreateGroup_fromGroupPostDTO_toGroup_success() {
		GroupPostDTO groupPostDTO = new GroupPostDTO();
		groupPostDTO.setName("Test Group");
		groupPostDTO.setPassword("secret");

		Group group = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(groupPostDTO);

		assertEquals(groupPostDTO.getName(), group.getName());
		assertEquals(groupPostDTO.getPassword(), group.getPassword());
	}

	@Test
	public void testGetGroup_fromGroup_toGroupGetDTO_success() {
		Group group = new Group();
		group.setId(1L);
		group.setName("Test Group");
		group.setCreatedBy("creator");
		group.setCreatedAt(java.time.LocalDateTime.now());

		User user = new User();
		user.setUsername("user1");
		group.getUsers().add(user);

		GroupGetDTO groupGetDTO = DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group);

		assertEquals(group.getId(), groupGetDTO.getId());
		assertEquals(group.getName(), groupGetDTO.getName());
		assertEquals(group.getCreatedBy(), groupGetDTO.getCreatedBy());
		assertEquals(group.getUsers().size(), groupGetDTO.getUsers().size());
	}

	@Test
	public void testConvertUserToGroupMember_mapsAllFields() {
		Character character = new Character();
		character.setLevel(3);

		User user = new User();
		user.setId(5L);
		user.setUsername("alice");
		user.setStatus(UserStatus.ONLINE);
		user.setCharacter(character);

		GroupGetDTO.GroupMember member = DTOMapper.INSTANCE.convertUserToGroupMember(user);

		assertEquals(5L, member.getId());
		assertEquals("alice", member.getUsername());
		assertEquals("ONLINE", member.getStatus());
		assertEquals(3, member.getLevel());
	}

	@Test
	public void testConvertHabit_toHabitGetDTO_mapsAllFields() {
		Habit habit = new Habit();
		habit.setId(1L);
		habit.setTitle("Morning Run");
		habit.setCategory(HabitCategory.PHYSICAL);
		habit.setFrequency(HabitFrequency.DAILY);
		habit.setPositive(true);
		habit.setWeight(2);
		habit.setCompleted(false);
		habit.setStreak(5);

		HabitGetDTO dto = DTOMapper.INSTANCE.convertEntityToHabitGetDTO(habit);

		assertEquals(1L, dto.getId());
		assertEquals("Morning Run", dto.getTitle());
		assertEquals(HabitCategory.PHYSICAL, dto.getCategory());
		assertEquals(HabitFrequency.DAILY, dto.getFrequency());
		assertEquals(true, dto.getPositive());
		assertEquals(2, dto.getWeight());
		assertEquals(false, dto.getCompleted());
		assertEquals(5, dto.getStreak());
	}

	@Test
	public void testConvertTodo_toTodoGetDTO_mapsAllFields() {
		Todo todo = new Todo();
		todo.setId(2L);
		todo.setTitle("Buy groceries");
		todo.setCategory(HabitCategory.COGNITIVE);
		todo.setWeight(1);
		todo.setCompleted(false);

		TodoGetDTO dto = DTOMapper.INSTANCE.convertEntityToTodoGetDTO(todo);

		assertEquals(2L, dto.getId());
		assertEquals("Buy groceries", dto.getTitle());
		assertEquals(HabitCategory.COGNITIVE, dto.getCategory());
		assertEquals(1, dto.getWeight());
		assertEquals(false, dto.getCompleted());
	}
}
