package com.irankiai.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfig(){
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200","https://irankiai.raksnys.lt")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }

        };
    }
    // Add OpenAPI bean to existing config class
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Irankiai API Documentation")
                        .description("API documentation for the Irankiai project")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Irankiai Team")
                                .url("https://irankiai.raksnys.lt")));
    }
}
