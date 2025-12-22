package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "bon")
@Getter
@Setter
@Check(constraints = "Type IN ('EXIT', 'ENTRY')")
@NoArgsConstructor
@AllArgsConstructor
public class Bon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateSortie;

    @Column(name = "Type", nullable = false)
    private String type;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false, unique = true)
    private Demandes demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magasinier_id", nullable = false)
    private Magasinier magasinier;

    @OneToMany(mappedBy = "bonSortie", cascade = CascadeType.ALL)
    private List<LigneBon> lignesBon;
}
