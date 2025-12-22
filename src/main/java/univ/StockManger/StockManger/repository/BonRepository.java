package univ.StockManger.StockManger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.StockManger.StockManger.entity.Bon;

import java.util.List;

public interface BonRepository extends JpaRepository<Bon, Long> {
    List<Bon> findByMagasinierId(Long magasinierId);
    List<Bon> findByType(String type);
    List<Bon> findByMagasinierIdAndType(Long magasinierId, String type);
}
