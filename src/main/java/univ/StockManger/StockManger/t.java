package univ.StockManger.StockManger;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class t {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("123456789");
        System.out.println(hashedPassword);
    }
}
