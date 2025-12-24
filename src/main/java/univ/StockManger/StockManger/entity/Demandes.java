package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandes") // Standardized to lowercase
@Getter
@Setter
//@Check(constraints = "etat_demande IN ('PENDING', 'DELIVERED', 'APPROVED', 'REJECTED')")
@NoArgsConstructor
@AllArgsConstructor
public class Demandes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id; // Standardized PK name

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Demandeur demandeur;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_demande", nullable = false)
    private RequestStatus etat_demande;

    private String commentaire;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneDemande> lignes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private SecretaireGeneral validatedBy;

    @PrePersist
    private void prePersist() {
        if (etat_demande == null) {
            etat_demande = RequestStatus.PENDING;
        }
    }


}
