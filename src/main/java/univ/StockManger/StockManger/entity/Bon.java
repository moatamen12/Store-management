//package univ.StockManger.StockManger.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.Check;
//import java.time.LocalDate;
//import java.util.List;
//
//@Entity
//@Table(name = "bon")
//@Getter
//@Setter
////@Check(constraints = "type IN ('EXIT', 'ENTRY')")
//@NoArgsConstructor
//@AllArgsConstructor
//public class Bon {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", updatable = false, nullable = false)
//    private Long id;
//
//    @Column(nullable = false)
//    private LocalDate date;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "type", nullable = false)
//    private ReceiptType type;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "demande_id", unique = true) // Removed nullable=false if not strictly required for all Bon types
//    private Demandes demande;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "magasinier_id", nullable = false)
//    private Magasinier magasinier;
//
//    @OneToMany(mappedBy = "bon", cascade = CascadeType.ALL)
//    private List<LigneBon> lignesBon;
//}
