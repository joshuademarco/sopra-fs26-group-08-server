package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Hat;
import ch.uzh.ifi.hase.soprafs26.entity.Item;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ItemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@WebAppConfiguration
@SpringBootTest
public class ItemServiceIntegrationTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ItemService itemService;

    private User user;
    private Hat hat;

    private void truncateAll() throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("TRUNCATE TABLE items");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        truncateAll();

        user = new User();
        user.setUsername("tester");
        user.setEmail("tester@test.com");
        user.setPassword("password");
        user.setToken("token");
        user.setStatus(UserStatus.OFFLINE);
        user = userRepository.save(user);

        hat = new Hat();
        hat.setName("Wizard Hat");
        hat.setAssetKey("wizard_1");
        hat = itemRepository.save(hat);
    }

    @AfterEach
    public void teardown() throws Exception {
        truncateAll();
    }

    @Test
    public void grantItem_validInputs_success() {
        itemService.grantItem(user.getId(), hat.getId());

        User updated = userRepository.findById(user.getId()).get();
        assertTrue(updated.hasItem(hat));
    }

    @Test
    public void getAllItems_returnsAllItems() {
        List<Item> items = itemService.getAllItems();

        assertEquals(1, items.size());
    }

    @Test
    public void getUserInventory_afterGrantItem_returnsItem() {
        itemService.grantItem(user.getId(), hat.getId());

        assertEquals(1, itemService.getUserInventory(user.getId()).size());
    }
}
