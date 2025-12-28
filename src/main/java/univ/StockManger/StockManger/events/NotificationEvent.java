package univ.StockManger.StockManger.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final NotificationType type;
    private final String message;
    private final Date createdAt;
    private final Long refId;
    private final Long userId;

    public NotificationEvent(Object source, NotificationType type, String message, Long refId, Long userId) {
        super(source);
        this.type = type;
        this.message = message;
        this.createdAt = new Date();
        this.refId = refId;
        this.userId = userId;
    }
}
