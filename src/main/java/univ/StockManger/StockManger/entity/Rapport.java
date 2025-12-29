package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "rapports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private ReportType reportType;

    @Column(name = "date_generation", nullable = false)
    private LocalDate dateGeneration;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_id", nullable = true)
    private User generatedBy;

    // For BY_USER reports
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    // For BY_PRODUCT reports
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_product_id")
    private Produits targetProduct;

    @Column(name = "total_expense")
    private Double totalExpense;

    @PrePersist
    protected void onCreate() {
        if (dateGeneration == null) {
            this.dateGeneration = LocalDate.now();
        }
    }
}
