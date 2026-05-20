package ch.uzh.ifi.hase.soprafs26.service;

import com.resend.Resend;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.User;

public class NotificationServiceTest {

    @Mock
    private Resend resend;
    
    @Mock
    private Emails emails;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        Mockito.when(resend.emails()).thenReturn(emails);
        Mockito.when(emails.send(any(CreateEmailOptions.class))).thenReturn(null);
        ReflectionTestUtils.setField(notificationService, "fromAddress", "test@example.com");
    }

    @Test
    void sendWelcomeEmail_callsSend() throws Exception {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        notificationService.sendWelcomeEmail(user);

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendRaidScheduledEmail_callsSend() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        BossRaid raid = new BossRaid();
        raid.setName("Dragon");
        Group group = new Group();
        group.setName("Heroes");

        notificationService.sendRaidScheduledEmail(user, raid, group);

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendRaidStartedEmail_callsSend() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        BossRaid raid = new BossRaid();
        raid.setName("Dragon");

        notificationService.sendRaidStartedEmail(user, raid);

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendRaidFinishedEmail_defeated_callsSend() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        BossRaid raid = new BossRaid();
        raid.setName("Dragon");
        raid.setStatus(RaidStatus.DEFEATED);

        notificationService.sendRaidFinishedEmail(user, raid);

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendUserJoinedGroupEmail_callsSend() throws Exception {
        User newMember = new User();
        newMember.setUsername("bob");
        newMember.setEmail("bob@example.com");
        Group group = new Group();
        group.setName("Heroes");

        notificationService.sendUserJoinedGroupEmail(newMember, group);

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }
}
