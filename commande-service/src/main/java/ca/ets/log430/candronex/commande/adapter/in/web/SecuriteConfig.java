package ca.ets.log430.candronex.commande.adapter.in.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Adaptateur d'entrée : chaque requête doit porter un JWT valide émis par Keycloak.
 */
@Configuration
public class SecuriteConfig {

    @Bean
    SecurityFilterChain securite(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(regles -> regles
                        .requestMatchers("/actuator/health").permitAll()   // sondes Docker, sans jeton
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API sans session
                .csrf(c -> c.disable())   // pas de cookie de session : CSRF ne s'applique pas
                .build();
    }
}