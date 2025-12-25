package univ.StockManger.StockManger.entity;

public enum Role {
    demandeur, magasinier ,Admin, Secretaire_General;

    public String authority() {
        return "ROLE_" + this.name();
    }
}
