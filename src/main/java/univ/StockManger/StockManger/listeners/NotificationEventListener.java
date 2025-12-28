package univ.StockManger.StockManger.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
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
            notificationRepository.save(notification);
        }
    }
}
