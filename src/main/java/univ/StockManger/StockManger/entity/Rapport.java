package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import java.time.LocalDate;

@Entity
@Table(name = "rapports")
//@Check(constraints = "type IN ('MONTHLY', 'BY_DEPARTMENT', 'BY_PRODUCT_TYPE')")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType report_type;

    @Column(nullable = false)
    private LocalDate dateGeneration;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double totalExpense;

    private String targetDepartment;

    private String targetCategory;

    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaire_general_id", nullable = false)
    private User secretaireGeneral;

    @PrePersist
    private void prePersist() {
        if (report_type == null) {
            report_type = ReportType.MONTHLY;
        }
    }

}






//package univ.StockManger.StockManger.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.Check;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "rapports")
////@Check(constraints = "type IN ('MONTHLY', 'BY_DEPARTMENT', 'BY_PRODUCT_TYPE')")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class Rapport {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", updatable = false, nullable = false)
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private ReportType report_type;
//
//    @Column(nullable = false)
//    private LocalDate dateGeneration;
//
//    private LocalDate startDate;
//
//    private LocalDate endDate;
//
//    private Double totalExpense;
//
//    private String targetDepartment;
//
//    private String targetCategory;
//
//    private String filePath;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "secretaire_general_id", nullable = false)
//    private SecretaireGeneral secretaireGeneral;
//
//    @PrePersist
//    private void prePersist() {
//        if (report_type == null) {
//            report_type = ReportType.MONTHLY;
//        }
//    }
//
//}
