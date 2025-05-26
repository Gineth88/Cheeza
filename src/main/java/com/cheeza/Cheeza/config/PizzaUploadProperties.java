
package com.cheeza.Cheeza.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pizza.upload")
@Data
public class PizzaUploadProperties {

    // Physical directory where files will be stored
    private String uploadDir = "uploads/pizza-images";

    // URL path used in HTML to reference the images
    private String imageUrlPath = "/pizza-images/";

    // Path to default placeholder image
    private String defaultImagePath = "images/placeholder2.jpeg";

    // Path to static resources directory
    private String staticResourcesPath = "src/main/resources/static";
}
