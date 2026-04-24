package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
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
}
