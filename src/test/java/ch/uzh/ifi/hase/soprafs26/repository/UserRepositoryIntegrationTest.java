package ch.uzh.ifi.hase.soprafs26.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@DataJpaTest
public class UserRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void findByName_success() {
		// given
		User user = new User();
		user.setEmail("testemail@uzh.ch");
		user.setPassword("password123");
		user.setUsername("testUsername");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("1");

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByEmail(user.getEmail());

		// then
		assertNotNull(found.getId());
		assertEquals(found.getEmail(), user.getEmail());
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getToken(), user.getToken());
		assertEquals(found.getStatus(), user.getStatus());
	}
}
