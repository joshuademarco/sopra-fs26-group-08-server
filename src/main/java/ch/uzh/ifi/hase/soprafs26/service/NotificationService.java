package ch.uzh.ifi.hase.soprafs26.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.constant.RaidStatus;
import ch.uzh.ifi.hase.soprafs26.entity.BossRaid;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@Service
public class NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final Resend resend;

    @Value("${resend.from-address}")
    private String fromAddress;

    @Configuration
    public class ResendConfig {
        @Bean
        public Resend resend(@Value("${resend.api-key}") String apiKey) {
            return new Resend(apiKey);
        }
    }

    public NotificationService(Resend resend) {
        this.resend = resend;
    }

    public void sendWelcomeEmail(User user) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(List.of(user.getEmail()))
                    .subject("Welcome to Better Together, " + user.getUsername() + "!")
                    .html("<p>Hey <b> " + user.getUsername()
                            + "</b>, welcome to Better Together! Your account is ready. Start completing habits and join a group to take on boss raids together.</p>")
                    .build();
            resend.emails().send(options);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendRaidScheduledEmail(User user, BossRaid raid, Group group) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(List.of(user.getEmail()))
                    .subject("Boss Raid Scheduled: " + raid.getName())
                    .html("<p>A boss raid has been scheduled for your group <b>" + group.getName()
                            + "</b>!</p><p>Raid: <b>" + raid.getName() + "</b><br>Time: <b>" + raid.getScheduledTime()
                            + "</b></p><p>Log in and get ready.</p>")
                    .build();
            resend.emails().send(options);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendRaidStartedEmail(User user, BossRaid raid) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(List.of(user.getEmail()))
                    .subject("Boss Raid Started: " + raid.getName())
                    .html("<p>The boss raid <b>" + raid.getName()
                            + "</b> has started! Log in now and complete your tasks before time runs out.</p>")
                    .build();
            resend.emails().send(options);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendRaidFinishedEmail(User user, BossRaid raid) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(List.of(user.getEmail()))
                    .subject(raid.getStatus() == RaidStatus.DEFEATED ? "Victory! ..." : "Defeat! ...")
                    .html(raid.getStatus() == RaidStatus.DEFEATED
                            ? "<p>Your group defeated <b>" + raid.getName()
                                    + "</b>! Great teamwork — check your rewards in the app.</p>"
                            : "<p>Your group could not defeat <b>" + raid.getName()
                                    + "</b> in time. Better luck on the next raid!</p>")
                    .build();
            resend.emails().send(options);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendUserJoinedGroupEmail(User newMember, Group group) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(List.of(newMember.getEmail()))
                    .subject(newMember.getUsername() + " joined " + group.getName() + "!")
                    .html("<p><b>" + newMember.getUsername() + "</b> just joined your group <b>" + group.getName()
                            + "</b>. Your squad is growing!</p>")
                    .build();
            resend.emails().send(options);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", newMember.getEmail(), e.getMessage());
        }
    }
}
