package univ.StockManger.StockManger.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.StockManger.StockManger.entity.Demandes;

public interface DemandesRepository extends JpaRepository<Demandes, Long> {

}
