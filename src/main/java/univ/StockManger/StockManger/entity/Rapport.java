package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import java.time.LocalDate;

@Entity
@Table(name = "rapports")
@Check(constraints = "type IN ('MONTHLY', 'BY_DEPARTMENT', 'BY_PRODUCT_TYPE')")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

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
    private SecretaireGeneral secretaireGeneral;
}
