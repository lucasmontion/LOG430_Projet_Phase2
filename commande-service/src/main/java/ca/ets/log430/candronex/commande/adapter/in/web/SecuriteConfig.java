package ca.ets.log430.candronex.commande.adapter.in.web;

import ca.ets.log430.candronex.commun.adapter.in.web.RefusNonAuthentifie;
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
    SecurityFilterChain securite(HttpSecurity http, RefusNonAuthentifie refus) throws Exception {
        return http
                .authorizeHttpRequests(regles -> regles
                        .requestMatchers("/actuator/health").permitAll()   // sondes Docker, sans jeton
                        // contrat OpenAPI et Swagger UI, sans jeton : le service n'est joignable que du réseau interne
                        .requestMatchers("/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/**",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(refus))   // 401 au format Problem Details
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API sans session
                .csrf(c -> c.disable())   // pas de cookie de session : CSRF ne s'applique pas
                .build();
    }
}