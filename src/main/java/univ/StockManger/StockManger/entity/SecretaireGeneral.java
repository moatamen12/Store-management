package univ.StockManger.StockManger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "secretaires_general")
@Getter
@Setter
@NoArgsConstructor
public class SecretaireGeneral extends User {

//    @Column(name = "sg_id")
//    private Long sgID;

    @OneToMany(mappedBy = "secretaireGeneral")
    private List<Rapport> rapports;

    @OneToMany(mappedBy = "validatedBy")
    private List<Demandes> validatedDemandes = new ArrayList<>();


//    @OneToMany(mappedBy = "recipient")
//    private List<Notification> notifications;
}
