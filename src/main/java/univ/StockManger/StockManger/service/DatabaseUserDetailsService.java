
package univ.StockManger.StockManger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Attempting to load user by email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        User user = userOptional.get();
        logger.info("User found: {}, Role: {}", user.getEmail(), user.getRole());
        if (user.getRole() == null) {
            logger.warn("User {} has no role assigned", user.getEmail());
            throw new UsernameNotFoundException("User has no role assigned: " + email);
        }

        return new CustomUserDetails(user);
    }

    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User {
        private final User user;

        public CustomUserDetails(User user) {
            super(user.getEmail(), user.getPassword(), getAuthorities(user));
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public long getId() {
            return user.getId();
            }

        private static Collection<? extends GrantedAuthority> getAuthorities(User user) {
            String granted = "ROLE_" + user.getRole().name().toUpperCase();
            return List.of(new SimpleGrantedAuthority(granted));
        }
    }
}
