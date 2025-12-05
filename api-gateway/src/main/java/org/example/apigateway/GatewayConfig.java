package org.example.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Маршрут для blood-request-service (включая шаблоны)
                .route("blood-request-service", r -> r
                        .path("/requests/**", "/templates/**", "/api/blood-requests/**", "/api/templates/**")
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                        )
                        .uri("lb://blood-request-service"))

                // Маршрут для user-service
                .route("user-service", r -> r
                        .path("/", "/register", "/login", "/home", "/logout", "/users/**")
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                        )
                        .uri("lb://user-service"))

                // Маршрут для med-center-service
                .route("med-center-service", r -> r
                        .path("/medcenters/**")
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                        )
                        .uri("lb://med-center-service"))

                // Маршрут для donor-service
                .route("donor-service", r -> r
                        .path("/donor/**")
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                        )
                        .uri("lb://donor-service"))

                // Маршрут для donation-history-service
                .route("donation-history-service", r -> r
                        .path("/donations/**", "/history/**")
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                        )
                        .uri("lb://donation-history-service"))

                // Маршрут для analysis-service
                .route("analysis-service", r -> r
                        .path("/analysis/**", "/reports/**")
                        .filters(f -> f
                                .rewritePath("/(?<segment>.*)", "/${segment}")
                        )
                        .uri("lb://analysis-service"))

                .build();
    }
}