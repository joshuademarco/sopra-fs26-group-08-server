package ch.uzh.ifi.hase.soprafs26.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    public void findByName_nonExistent_returnsNull() {
        Group found = groupRepository.findByName("DoesNotExist");

        assertNull(found);
    }

    @Test
    public void saveGroup_persistsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        Group group = new Group();
        group.setName("Persist Group");
        group.setPassword("pass123");
        group.setCreatedBy("owner");
        group.setCreatedAt(now);

        entityManager.persist(group);
        entityManager.flush();
        entityManager.clear();

        Optional<Group> found = groupRepository.findById(group.getId());

        assertTrue(found.isPresent());
        assertEquals("Persist Group", found.get().getName());
        assertEquals("pass123", found.get().getPassword());
        assertEquals("owner", found.get().getCreatedBy());
        assertNotNull(found.get().getCreatedAt());
    }

    @Test
    public void findById_existingGroup_returnsGroup() {
        Group group = new Group();
        group.setName("Find By Id Group");
        group.setPassword("secret");
        group.setCreatedBy("testOwner");

        entityManager.persist(group);
        entityManager.flush();

        Optional<Group> found = groupRepository.findById(group.getId());

        assertTrue(found.isPresent());
        assertEquals("Find By Id Group", found.get().getName());
    }
}
