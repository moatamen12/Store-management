package univ.StockManger.StockManger.Controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;
    private final MessageSource messageSource;

    public CustomErrorController(ErrorAttributes errorAttributes, MessageSource messageSource) {
        this.errorAttributes = errorAttributes;
        this.messageSource = messageSource;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, WebRequest webRequest, Model model, Locale locale) {
        Map<String, Object> attrs = errorAttributes.getErrorAttributes(
                webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        );
        model.addAttribute("timestamp", attrs.getOrDefault("timestamp", ""));
        model.addAttribute("status", attrs.getOrDefault("status", 500));
        model.addAttribute("error", attrs.getOrDefault("error", messageSource.getMessage("error.internalServerError", null, locale)));
        model.addAttribute("message", attrs.getOrDefault("message", messageSource.getMessage("error.unexpected", null, locale)));
        model.addAttribute("path", attrs.getOrDefault("path", request.getRequestURI()));
        return "error";
    }
}
