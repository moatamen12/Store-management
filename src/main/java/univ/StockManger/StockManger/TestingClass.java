package univ.StockManger.StockManger;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class TestingClass {
    @GetMapping()
    public String hello(){
        return "hello World";
    }
}
