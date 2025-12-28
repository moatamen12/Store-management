// java
package univ.StockManger.StockManger.Repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import univ.StockManger.StockManger.entity.Demandes;
import univ.StockManger.StockManger.entity.RequestStatus;
import univ.StockManger.StockManger.entity.User;


public interface DemandesRepository extends JpaRepository<Demandes, Long> {

    List<Demandes> findByDemandeur(User demandeur);

    @Query(value = "SELECT * FROM demandes WHERE demandeur_id = :demandeurId ORDER BY request_date DESC LIMIT 10", nativeQuery = true)
    List<Demandes> findTop10RecentForDemandeur(@Param("demandeurId") Long demandeurId);

    @Query("SELECT d FROM Demandes d WHERE d.etat_demande = :status")
    List<Demandes> findByEtatDemande(@Param("status") RequestStatus status);
}
