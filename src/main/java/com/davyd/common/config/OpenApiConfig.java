package com.davyd.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flowBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowBank API")
                        .description(
                                "REST API for managing users, bank accounts "
                                        + "and money transactions with auth functionality"
                        )
                        .version("2.0.0")
                );
    }
}
