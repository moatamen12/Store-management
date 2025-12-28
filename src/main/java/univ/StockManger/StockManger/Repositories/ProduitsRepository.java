package univ.StockManger.StockManger.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import univ.StockManger.StockManger.entity.Produits;

public interface ProduitsRepository extends JpaRepository<Produits, Long> {
    Page<Produits> findByNomContainingIgnoreCase(String nom, Pageable pageable);

    @Query("SELECT p FROM Produits p WHERE p.quantite <= p.seuilAlerte")
    Page<Produits> findLowStock(Pageable pageable);
}
