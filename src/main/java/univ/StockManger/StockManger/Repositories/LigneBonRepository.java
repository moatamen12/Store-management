package univ.StockManger.StockManger.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import univ.StockManger.StockManger.entity.LigneBon;

@Repository
public interface LigneBonRepository extends JpaRepository<LigneBon, Long> {
}
