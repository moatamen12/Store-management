package univ.StockManger.StockManger.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class SgController {
    @GetMapping("/sg")
    public String sgDashboard() {
        return "sg";
    }
}
