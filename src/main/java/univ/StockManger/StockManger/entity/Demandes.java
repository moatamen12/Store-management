package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Demandes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    // EAGER so the view can read demandeur fields without LazyInitializationException
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "demandeur_id", nullable = true)
    private User demandeur;

    @Column(name = "request_date", nullable = false)
    private LocalDate request_date;

    @Column(name = "validation_date", nullable = false)
    private LocalDate validation_date;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_demande", nullable = false)
    private RequestStatus etat_demande;

    private String commentaire;

    // EAGER so the view can iterate lines and access nested product fields
    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LigneDemande> lignes = new ArrayList<>();

    // EAGER to allow access to validator info in views without initializing proxy later
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "validated_by_id")
    private User validatedBy;

    @PrePersist
    private void prePersist() {
        if (etat_demande == null) {
            etat_demande = RequestStatus.PENDING;
        }
    }
}
