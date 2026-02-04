package univ.StockManger.StockManger.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import univ.StockManger.StockManger.entity.Bon;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BonRepository extends JpaRepository<Bon, Long>, JpaSpecificationExecutor<Bon> {
    @Query("SELECT b FROM Bon b WHERE b.date >= :startDate AND b.date <= :endDate")
    List<Bon> findAllByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT b FROM Bon b LEFT JOIN FETCH b.lignesBon lb LEFT JOIN FETCH lb.produit WHERE b.date BETWEEN :startDate AND :endDate")
    List<Bon> findAllWithLignesAndProduitsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
