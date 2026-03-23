package com.remotejobs.notification;

import com.remotejobs.entity.Subscription;
import com.remotejobs.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SubscriptionRepository subscriptionRepo;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.telegram.enabled:false}")
    private boolean telegramEnabled;

    @Value("${notification.telegram.bot-token:}")
    private String botToken;

    @Value("${notification.email.recipient:}")
    private String defaultRecipient;

    public void notifyNewJobs(int count) {
        if (count == 0) return;

        String subject = "🚀 " + count + " new remote jobs found!";
        String body = count + " new remote jobs were aggregated. Check your dashboard.";

        List<Subscription> subs = subscriptionRepo.findByActiveTrue();

        if (subs.isEmpty()) {
            // Fall back to default config
            sendDefaultNotifications(subject, body);
            return;
        }

        for (Subscription sub : subs) {
            try {
                if (sub.getChannel() == Subscription.Channel.EMAIL) {
                    sendEmail(sub.getDestination(), subject, body);
                } else if (sub.getChannel() == Subscription.Channel.TELEGRAM) {
                    sendTelegram(sub.getDestination(), subject + "\n" + body);
                }
            } catch (Exception e) {
                log.warn("[Notification] Failed to notify {}: {}", sub.getDestination(), e.getMessage());
            }
        }
    }

    private void sendDefaultNotifications(String subject, String body) {
        if (emailEnabled && !defaultRecipient.isBlank()) {
            sendEmail(defaultRecipient, subject, body);
        }
    }

    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) { log.debug("[Email] Disabled"); return; }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[Email] Sent to {}", to);
        } catch (Exception e) {
            log.error("[Email] Failed to send: {}", e.getMessage());
        }
    }

    public void sendTelegram(String chatId, String message) {
        if (!telegramEnabled || botToken.isBlank()) { log.debug("[Telegram] Disabled"); return; }
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            var body = new java.util.HashMap<String, String>();
            body.put("chat_id", chatId);
            body.put("text", message);
            body.put("parse_mode", "HTML");
            restTemplate.postForObject(url, body, String.class);
            log.info("[Telegram] Sent to chatId {}", chatId);
        } catch (Exception e) {
            log.error("[Telegram] Failed: {}", e.getMessage());
        }
    }
}