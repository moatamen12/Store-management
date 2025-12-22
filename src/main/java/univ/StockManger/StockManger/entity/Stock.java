package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private List<Produits> products = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY) // Changed to ManyToOne
    @JoinColumn(name = "magasinier_id")
    private Magasinier magasinier;
}
