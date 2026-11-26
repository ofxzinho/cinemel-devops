package com.cinemel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

@Configuration(proxyBeanMethods = false)
class ApiDocumentation {

    @Bean
    OpenAPI cinemelOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CineMel")
                .version("1.0")
                .description("API de filmes, salas, assentos, sessões e reservas do CineMel."));
    }

    @Bean
    SwaggerIndexPageTransformer cinemelSwaggerTheme(
            SwaggerUiConfigProperties config, SwaggerUiOAuthProperties oauth,
            SwaggerWelcomeCommon welcome, ObjectMapperProvider mapper) {
        return new SwaggerIndexPageTransformer(config, oauth, welcome, mapper) {
            @Override
            public Resource transform(HttpServletRequest request, Resource resource,
                    ResourceTransformerChain chain) throws IOException {
                Resource transformed = super.transform(request, resource, chain);
                if (!"index.html".equals(resource.getFilename())) {
                    return transformed;
                }

                String html = transformed.getContentAsString(StandardCharsets.UTF_8)
                        .replace("<html lang=\"en\">", "<html lang=\"pt-BR\">")
                        .replace("<title>Swagger UI</title>", "<title>CineMel | Console da API</title>")
                        .replace("</head>",
                                "<link rel=\"stylesheet\" href=\"../cinemel-docs.css\">\n</head>");
                return new TransformedResource(transformed, html.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
}
