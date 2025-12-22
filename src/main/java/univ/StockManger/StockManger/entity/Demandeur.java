package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandeurs")
@Getter
@Setter
@NoArgsConstructor
public class Demandeur extends User {

    @OneToMany(mappedBy = "demandeur", cascade = CascadeType.ALL)
    private List<Demandes> demandes = new ArrayList<>();
}
