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

/*
  Repository for Demandes:
  - use fetch joins to eagerly load lines and products for templates (avoid LazyInitializationException)
  - provide status-based query
  - provide pageable recent query (controller can pass PageRequest.of(0, 8))
*/
public interface DemandesRepository extends JpaRepository<Demandes, Long> {

//    /* Simple derived query (keeps backward compatibility if your entity field is named `etat_demande`) */
//    List<Demandes> findByEtat_demande(RequestStatus status);
//
//    /* Explicit JPQL: load demandes with their lines and products to avoid lazy loading in the view */
//    @Query("select distinct d from Demandes d " +
//            "left join fetch d.lignes l " +
//            "left join fetch l.produit p " +
//            "left join fetch d.demandeur")
//    List<Demandes> findAllWithLinesAndProducts();
//
//    /* Status-filtered with fetch joins and ordered by date desc */
//    @Query("select distinct d from Demandes d " +
//            "left join fetch d.lignes l " +
//            "left join fetch l.produit p " +
//            "left join fetch d.demandeur " +
//            "where d.etat_demande = :status " +
//            "order by d.date desc")
//    List<Demandes> findByEtatDemandeWithLinesOrderByDateDesc(@Param("status") RequestStatus status);
//
//    /* Pageable recent list (pass PageRequest.of(0,8) from controller to get top 8 recent). */
//    @Query("select distinct d from Demandes d " +
//            "left join fetch d.lignes l " +
//            "left join fetch l.produit p " +
//            "left join fetch d.demandeur " +
//            "order by d.date desc")
//    List<Demandes> findRecentWithLinesOrderByDateDesc(Pageable pageable);

    List<Demandes> findByDemandeur(User demandeur);
}
