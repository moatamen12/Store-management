package univ.StockManger.StockManger;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Get the user's authorities (roles)
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin");
                return;
            } else if (authority.getAuthority().equals("ROLE_SECRETAIRE_GENERAL")) {
                response.sendRedirect("/sg");
                return;
            }else if (authority.getAuthority().equals("ROLE_DEMANDEUR")) {
                response.sendRedirect("/requester");
                return;
            }else if (authority.getAuthority().equals("ROLE_MAGASINIER")) {
                response.sendRedirect("/magasinier");
                return;
            }
        }
        // Default redirect if no matching role
        response.sendRedirect("/login?error");
    }
}
