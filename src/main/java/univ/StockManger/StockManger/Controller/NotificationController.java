package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import univ.StockManger.StockManger.Repositories.NotificationRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.Notification;
import univ.StockManger.StockManger.entity.User;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Notification> getNotifications(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                return notificationRepository.findByUserOrderByCreatedAtDesc(user);
            }
        }
        return Collections.emptyList();
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<Void> markAsRead(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                List<Notification> notifications = notificationRepository.findByUserAndIsRead(user, false);
                notifications.forEach(n -> n.setRead(true));
                notificationRepository.saveAll(notifications);
            }
        }
        return ResponseEntity.ok().build();
    }
}
