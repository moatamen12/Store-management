package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import univ.StockManger.StockManger.events.NotificationType;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NotificationType type;

    private String message;

    private Date createdAt;

    private Long refId;

    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
