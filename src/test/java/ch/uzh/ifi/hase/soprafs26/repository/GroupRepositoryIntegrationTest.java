package ch.uzh.ifi.hase.soprafs26.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.entity.Group;

@DataJpaTest
public class GroupRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    public void findByName_success() {
        Group group = new Group();
        group.setName("Test Group");
        group.setPassword("secret");
        group.setCreatedBy("groupOwner");

        entityManager.persist(group);
        entityManager.flush();

        Group found = groupRepository.findByName(group.getName());

        assertNotNull(found.getId());
        assertEquals(group.getName(), found.getName());
        assertEquals(group.getPassword(), found.getPassword());
        assertEquals(group.getCreatedBy(), found.getCreatedBy());
    }
}
