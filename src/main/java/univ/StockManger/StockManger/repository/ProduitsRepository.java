package univ.StockManger.StockManger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.StockManger.StockManger.entity.Produits;

public interface ProduitsRepository extends JpaRepository<Produits, Long> {
    boolean existsByNom(String nom);
}
