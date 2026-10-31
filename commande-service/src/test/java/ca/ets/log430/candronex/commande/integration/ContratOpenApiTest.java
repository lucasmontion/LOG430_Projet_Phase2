package ca.ets.log430.candronex.commande.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrat OpenAPI : généré à partir des contrôleurs, puis exporté dans openapi/commande-service.yaml
 * (racine du dépôt). Chaque mvn test régénère le fichier livré : il ne peut pas diverger du code.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContratOpenApiTest {
    static final Path FICHIER = Path.of("..", "openapi", "commande-service.yaml");

    /** YAML lisible, où une chaîne qui ressemble à un nombre (ex. un IMSI) reste entre guillemets. */
    static final ObjectMapper YAML = new ObjectMapper(YAMLFactory.builder()
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .disable(YAMLGenerator.Feature.SPLIT_LINES)
            .build());

    @Autowired MockMvc mvc;

    @MockitoBean JwtDecoder decodeurJwt;

    @Test
    void leContratEstServiSansJetonEtExporte() throws Exception {
        String json = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode contrat = new ObjectMapper().readTree(json);

        assertThat(contrat.path("paths").has("/api/commande-service/v1/commande")).isTrue();
        assertThat(contrat.path("paths").has("/api/commande-service/v1/commande/{commandeId}")).isTrue();
        assertThat(contrat.at("/components/schemas/ProblemDetails").isMissingNode()).isFalse();
        assertThat(contrat.at("/components/securitySchemes/jetonBearer/scheme").asText()).isEqualTo("bearer");

        Files.createDirectories(FICHIER.getParent());
        Files.writeString(FICHIER, YAML.writeValueAsString(contrat), StandardCharsets.UTF_8);
    }

    @Test
    void swaggerUiEstAccessibleSansJeton() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
