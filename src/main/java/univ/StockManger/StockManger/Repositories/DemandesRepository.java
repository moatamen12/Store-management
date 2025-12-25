package univ.StockManger.StockManger.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import univ.StockManger.StockManger.entity.Demandes;

public interface DemandesRepository extends JpaRepository<Demandes, Long> {

//    @Query("select distinct d from Demandes d " +
//            "left join fetch d.lignes l " +
//            "left join fetch l.produit p")
//    List<Demandes> findAllWithLinesAndProducts();
}