package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Magasinier")
@Getter
@Setter
public class Magasinier extends User {

    @OneToMany(mappedBy = "magasinier", cascade = CascadeType.ALL)
    private List<Bon> bons = new ArrayList<>();

    @OneToMany(mappedBy = "magasinier")
    private List<Stock> stocks = new ArrayList<>();
}
