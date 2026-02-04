package univ.StockManger.StockManger.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import univ.StockManger.StockManger.entity.Rapport;
import univ.StockManger.StockManger.entity.ReportType;

import java.time.LocalDate;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, Long> {
    @Query("SELECT r FROM Rapport r WHERE CAST(r.reportType AS string) LIKE %:keyword%")
    Page<Rapport> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByReportTypeAndStartDateAndEndDate(ReportType reportType, LocalDate startDate, LocalDate endDate);
}
