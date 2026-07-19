package com.smartmess.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI smartMessOpenAPI() {

        return new OpenAPI()

                .info(

                        new Info()

                                .title("Smart Mess API")

                                .version("1.0")

                                .description(
                                        "Backend REST APIs for Smart Mess System"
                                )

                                .contact(

                                        new Contact()

                                                .name("Saurabh Dixit")

                                                .email("saurabhdixit9890@gmail.com")
                                )
                );
    }
}