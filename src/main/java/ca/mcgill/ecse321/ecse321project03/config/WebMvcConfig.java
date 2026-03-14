package ca.mcgill.ecse321.ecse321project03.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures static resource handling. Exposes uploaded item images under
 * /images/** so that paths like /images/items/1/uuid.jpg resolve to files in
 * the uploads/ directory (e.g. uploads/items/1/uuid.jpg). This allows the
 * frontend to load images using the URLs returned in ItemDefinitionResponseDto.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images: /images/** -> uploads/** (relative to working directory)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:./uploads/");
    }
}
