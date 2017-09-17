package be.echostyle.moola.spring;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class OpenCorsConfiguration implements CorsConfigurationSource {

    private final CorsConfiguration config;

    public OpenCorsConfiguration() {
        config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(CorsConfiguration.ALL));
        config.setAllowedHeaders(Arrays.asList(CorsConfiguration.ALL));
        config.setAllowedMethods(Arrays.asList(CorsConfiguration.ALL));
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        return config;
    }
}
