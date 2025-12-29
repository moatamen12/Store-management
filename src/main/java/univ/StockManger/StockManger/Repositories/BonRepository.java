package univ.StockManger.StockManger.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import univ.StockManger.StockManger.entity.Bon;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BonRepository extends JpaRepository<Bon, Long>, JpaSpecificationExecutor<Bon> {
    List<Bon> findAllByDateBetween(LocalDate startDate, LocalDate endDate);
}
