package univ.StockManger.StockManger.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import univ.StockManger.StockManger.Repositories.NotificationRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.Notification;
import univ.StockManger.StockManger.entity.User;
import univ.StockManger.StockManger.events.NotificationEvent;

@Component
public class NotificationEventListener {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // For sending WebSocket messages

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user != null) {
            Notification notification = Notification.builder()
                    .type(event.getType())
                    .message(event.getMessage())
                    .createdAt(event.getCreatedAt())
                    .refId(event.getRefId())
                    .user(user)
                    .build();
            
            // 1. Save the notification to the database
            notificationRepository.save(notification);

            // 2. Broadcast the notification to the user-specific topic
            // The client will subscribe to "/topic/notifications/{userId}"
            String userTopic = "/topic/notifications/" + user.getId();
            messagingTemplate.convertAndSend(userTopic, notification);
        }
    }
}
