package univ.StockManger.StockManger.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import univ.StockManger.StockManger.entity.Produits;
import univ.StockManger.StockManger.entity.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ProduitsRepository extends JpaRepository<Produits, Long> {
    Page<Produits> findByNomContainingIgnoreCase(String nom, Pageable pageable);
    Optional<Produits> getProduitById(Long id);
    @Query("SELECT p FROM Produits p WHERE p.quantite <= p.seuilAlerte")
    Page<Produits> findLowStock(Pageable pageable);

    @Query("SELECT COUNT(ld) FROM LigneDemande ld WHERE ld.produit.id = :productId AND ld.demande.etat_demande = :status")
    long countActiveRequestsForProduct(@Param("productId") Long productId, @Param("status") RequestStatus status);

    @Query("SELECT COUNT(ld) FROM LigneDemande ld WHERE ld.produit.id = :productId AND ld.demande.etat_demande IN :statuses")
    long countActiveRequestsForProductInStatus(@Param("productId") Long productId, @Param("statuses") List<RequestStatus> statuses);
}
