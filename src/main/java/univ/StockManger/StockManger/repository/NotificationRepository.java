//package univ.StockManger.StockManger.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import univ.StockManger.StockManger.entity.Notification;
//
//import java.util.List;
//
//public interface NotificationRepository extends JpaRepository<Notification, Long> {
//    List<Notification> findByRecipientIdAndLuFalse(Long recipientId);
//    List<Notification> findByRecipientId(Long recipientId);
//}
