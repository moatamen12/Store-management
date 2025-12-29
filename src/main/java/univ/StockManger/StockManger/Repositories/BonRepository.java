package univ.StockManger.StockManger.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import univ.StockManger.StockManger.entity.Bon;

@Repository
public interface BonRepository extends JpaRepository<Bon, Long>, JpaSpecificationExecutor<Bon> {
}
