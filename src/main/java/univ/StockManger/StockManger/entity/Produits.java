package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE Produits SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
public class Produits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private int quantite;

    private int seuilAlerte;

    private double prixUnitaire;

    @OneToMany(mappedBy = "produit")
    private List<LigneBon> lignesBon = new ArrayList<>();

    private boolean deleted = false;

}
