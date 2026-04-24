package ch.uzh.ifi.hase.soprafs26.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GroupEntityTest {

    @Test
    public void addUser_addsBiDirectionalRelationship() {
        Group group = new Group();
        group.setName("Test Group");

        User user = new User();
        user.setUsername("user1");

        group.addUser(user);

        assertTrue(group.getUsers().contains(user));
        assertTrue(user.getGroups().contains(group));
    }

    @Test
    public void removeUser_removesBiDirectionalRelationship() {
        Group group = new Group();
        group.setName("Test Group");

        User user = new User();
        user.setUsername("user1");

        group.addUser(user);
        group.removeUser(user);

        assertFalse(group.getUsers().contains(user));
        assertFalse(user.getGroups().contains(group));
    }
}
