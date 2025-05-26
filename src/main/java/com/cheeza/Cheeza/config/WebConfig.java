package com.cheeza.Cheeza.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final PizzaUploadProperties pizzaUploadProperties;



    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(pizzaUploadProperties.getImageUrlPath() + "**")
                .addResourceLocations("file:" + pizzaUploadProperties.getUploadDir() + "/")
                .setCachePeriod(0);
    }
}
