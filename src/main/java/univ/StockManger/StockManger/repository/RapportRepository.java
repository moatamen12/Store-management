package univ.StockManger.StockManger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.StockManger.StockManger.entity.Rapport;

import java.util.List;

public interface RapportRepository extends JpaRepository<Rapport, Long> {
    List<Rapport> findBySecretaireGeneralId(Long secretaireId);
    List<Rapport> findByType(String type);
}
