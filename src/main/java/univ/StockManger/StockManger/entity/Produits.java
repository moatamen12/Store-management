package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    private List<LigneBon> lignesBon = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;
}





//package univ.StockManger.StockManger.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "Produits")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class Produits {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", updatable = false, nullable = false)
//    private Long id;
//
//    @Column(nullable = false)
//    private String nom;
//
//    @Column(nullable = false)
//    private int quantite;
//
//    private int seuilAlerte;
//
//    private double prixUnitaire;
//
//    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
//    private List<LigneBon> lignesBon = new ArrayList<>();
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "stock_id")
//    private Stock stock;
//}
