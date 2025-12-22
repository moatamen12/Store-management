package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Demandes")
@Getter
@Setter
@Check(constraints = "EtatDemande IN ('PENDING', 'DELIVERED', 'APPROVED', 'REJECTED')")
@NoArgsConstructor
@AllArgsConstructor
public class Demandes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long demandeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Demandeur demandeur;

    @Column(nullable = false)
    private LocalDate date;


    @Column(name = "EtatDemande", nullable = false)
    private String EtatDemande;

    private String commentaire;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneDemande> lignes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private SecretaireGeneral validatedBy;
}
