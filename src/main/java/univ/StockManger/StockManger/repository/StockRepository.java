package univ.StockManger.StockManger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.StockManger.StockManger.entity.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProduitId(Long produitId);
    List<Stock> findByQuantiteDisponibleLessThanEqual(int seuilAlerte);
}
