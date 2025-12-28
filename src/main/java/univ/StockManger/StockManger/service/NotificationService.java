package univ.StockManger.StockManger.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import univ.StockManger.StockManger.events.NotificationEvent;
import univ.StockManger.StockManger.events.NotificationType;

@Service
public class NotificationService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void createNotification(Object source, NotificationType type, String message, Long refId, Long userId) {
        eventPublisher.publishEvent(new NotificationEvent(source, type, message, refId, userId));
    }
}
