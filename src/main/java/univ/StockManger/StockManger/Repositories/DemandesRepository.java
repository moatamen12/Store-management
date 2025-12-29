package univ.StockManger.StockManger.Repositories;

import org.springframework.data.domain.Page;
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

    @Query("SELECT d FROM Demandes d LEFT JOIN FETCH d.lignes l LEFT JOIN FETCH l.produit WHERE d.demandeur.id = :demandeurId ORDER BY d.request_date DESC")
    List<Demandes> findTop10RecentForDemandeur(@Param("demandeurId") Long demandeurId, Pageable pageable);

    @Query("SELECT d FROM Demandes d WHERE d.etat_demande = :status")
    List<Demandes> findByEtatDemande(@Param("status") RequestStatus status);

    @Query("SELECT d FROM Demandes d WHERE d.etat_demande = :status ORDER BY d.request_date DESC")
    List<Demandes> findTop10ByEtatDemandeOrderByRequest_dateDesc(@Param("status") RequestStatus status, Pageable pageable);

    @Query(value = "SELECT DISTINCT d FROM Demandes d LEFT JOIN FETCH d.lignes l LEFT JOIN FETCH l.produit WHERE d.demandeur.id = :demandeurId ORDER BY d.request_date DESC",
           countQuery = "SELECT COUNT(d) FROM Demandes d WHERE d.demandeur.id = :demandeurId")
    Page<Demandes> findByDemandeurIdOrderByRequest_dateDesc(@Param("demandeurId") Long demandeurId, Pageable pageable);

    @Query(value = "SELECT DISTINCT d FROM Demandes d LEFT JOIN FETCH d.lignes l LEFT JOIN FETCH l.produit p WHERE d.demandeur.id = :demandeurId AND p.nom LIKE %:search% ORDER BY d.request_date DESC",
           countQuery = "SELECT COUNT(DISTINCT d) FROM Demandes d JOIN d.lignes l JOIN l.produit p WHERE d.demandeur.id = :demandeurId AND p.nom LIKE %:search%")
    Page<Demandes> findByDemandeurIdAndLignesProduitNomContainingIgnoreCaseOrderByRequest_dateDesc(@Param("demandeurId") Long demandeurId, @Param("search") String search, Pageable pageable);

    @Query("SELECT d FROM Demandes d WHERE d.etat_demande IN :statuses ORDER BY d.request_date DESC")
    List<Demandes> findByEtat_demandeInOrderByRequest_dateDesc(@Param("statuses") List<RequestStatus> statuses);

    @Query("SELECT d FROM Demandes d WHERE d.etat_demande IN :statuses ORDER BY d.request_date DESC")
    List<Demandes> findTop10ByEtat_demandeInOrderByRequest_dateDesc(@Param("statuses") List<RequestStatus> statuses, Pageable pageable);

    long countByDemandeurId(Long demandeurId);
    
    @Query("SELECT count(d) FROM Demandes d WHERE d.demandeur.id = :demandeurId AND d.etat_demande = :etatDemande")
    long countByDemandeurIdAndEtat_demande(@Param("demandeurId") Long demandeurId, @Param("etatDemande") RequestStatus etatDemande);

    @Query("SELECT d FROM Demandes d WHERE d.etat_demande IN ('PENDING', 'APPROVED') OR (d.etat_demande = 'DELIVERED' AND d.bon.magasinier = :magasinier) ORDER BY CASE WHEN d.etat_demande = 'DELIVERED' THEN d.bon.date ELSE d.request_date END DESC")
    List<Demandes> findMagasinierRequests(@Param("magasinier") User magasinier);
}
