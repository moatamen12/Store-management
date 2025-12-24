package univ.StockManger.StockManger.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admins")
@Getter
@Setter
public class Admin extends User{

//        @Column(name = "adminID")
//        private long adminID;
}
