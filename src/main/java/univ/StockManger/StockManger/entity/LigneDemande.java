package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LigneDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigneDemande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande")
    private Demandes demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Produits produit;

    private int quantiteDemandee;
}
