package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lignes_bon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneBon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private int quantite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produits produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_id")
    private Bon bon;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "bon_entree_id")
//    private BonEntree bonEntree;
}
