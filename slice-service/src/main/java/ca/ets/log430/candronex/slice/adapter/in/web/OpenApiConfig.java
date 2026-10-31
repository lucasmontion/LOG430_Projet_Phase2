package ca.ets.log430.candronex.slice.adapter.in.web;

import ca.ets.log430.candronex.commun.adapter.in.web.ContratOpenApi;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Contrat OpenAPI de slice-service (servi à /v3/api-docs, exporté dans openapi/slice-service.yaml). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI contratSliceService() {
        OpenAPI contrat = ContratOpenApi.base("slice-service",
                "Contexte Activation et inventaire des services (UC-06). L'activation auprès du réseau 5G "
                        + "(simulé) se fait en arrière-plan. Les routes marquées « Interne » ne sont pas exposées "
                        + "par la passerelle.",
                "http://slice-service:8083");
        ContratOpenApi.erreur(contrat.getComponents(), "ServiceIntrouvable", 404,
                "Service inexistant OU appartenant à un autre client (indiscernables). Code : SERVICE_INTROUVABLE",
                "SERVICE_INTROUVABLE", "Service 4d8e2b1f-7a3c-4e95-8b06-1c9f5a2d7e40 introuvable");
        return contrat;
    }
}
