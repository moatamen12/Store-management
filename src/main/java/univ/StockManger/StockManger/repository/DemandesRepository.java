package univ.StockManger.StockManger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.StockManger.StockManger.entity.Demandes;

import java.util.List;

public interface DemandesRepository extends JpaRepository<Demandes, Long> {
    List<Demandes> findByDemandeurId(Long demandeurId);
    List<Demandes> findByStatut(String statut);
}
