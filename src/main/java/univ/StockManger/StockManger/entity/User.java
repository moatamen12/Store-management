package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users") // Standardized to lowercase
//@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    protected Long id;

    @Column(nullable = false, unique = true)
    @NotEmpty
    private String email;

    @Column(nullable = false)
    @NotEmpty
    private String password;

    @Column(nullable = false)
    @NotEmpty
    private String nom;

    @Column(nullable = false)
    @NotEmpty
    private String prenom;

//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    @Column(name = "role")
//    @NotEmpty
//    private Set<Role> roles;
//
//    public Set<Role> getRoles() { return roles; }
//    public void setRoles(Set<Role> roles) { this.roles = roles; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Role role;


    // From Demandeur
    @OneToMany(mappedBy = "demandeur", cascade = CascadeType.ALL)
    private List<Demandes> demandes = new ArrayList<>();

    // From SecretaireGeneral
    @OneToMany(mappedBy = "secretaireGeneral")
    private List<Rapport> rapports;

    @OneToMany(mappedBy = "validatedBy")
    private List<Demandes> validatedDemandes = new ArrayList<>();

    // From Magasinier
    @OneToMany(mappedBy = "magasinier", cascade = CascadeType.ALL)
    private List<Bon> bons = new ArrayList<>();

    @OneToMany(mappedBy = "magasinier")
    private List<Stock> stocks = new ArrayList<>();
}
