package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "User")
@Inheritance(strategy = InheritanceType.JOINED)
@Check(constraints = "role IN ('SECRETAIRE_GENERAL', 'MAGASINIER', 'DEMANDEUR', 'ADMIN')")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Userid;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "role")
    private String role;
}
