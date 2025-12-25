package univ.StockManger.StockManger.Controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, WebRequest webRequest, Model model) {
        Map<String, Object> attrs = errorAttributes.getErrorAttributes(
                webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        );
        model.addAttribute("timestamp", attrs.getOrDefault("timestamp", ""));
        model.addAttribute("status", attrs.getOrDefault("status", 500));
        model.addAttribute("error", attrs.getOrDefault("error", "Internal Server Error"));
        model.addAttribute("message", attrs.getOrDefault("message", "Unexpected error"));
        model.addAttribute("path", attrs.getOrDefault("path", request.getRequestURI()));
        return "error";
    }
}
