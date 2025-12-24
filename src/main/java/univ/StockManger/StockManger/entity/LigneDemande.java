package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lignes_demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private int quantiteDemandee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demandes demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produits produit;
}
