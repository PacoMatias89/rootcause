package com.rootcause.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

/**
 * Spring configuration class for application-wide infrastructure beans.
 *
 * <p>This configuration provides shared infrastructure concerns used across the
 * application, including:</p>
 *
 * <ul>
 *     <li>a shared {@link Clock} bean for deterministic UTC-based time access</li>
 *     <li>a global CORS configuration for frontend integration during development</li>
 * </ul>
 *
 * <p>Using a single clock bean improves consistency in time-based operations and avoids
 * coupling the code to the default system clock directly. It also makes testing easier,
 * because the clock can be replaced or controlled in test scenarios.</p>
 *
 * <p>The configured clock uses UTC so that persisted timestamps and time-based logic do not
 * depend on the server local timezone.</p>
 *
 * <p>The global CORS configuration is centralized here to keep HTTP integration concerns
 * out of individual controllers and to prepare the backend for a separate frontend
 * application.</p>
 */
@Configuration
public class ApplicationConfig {

    /**
     * Frontend origin allowed during local development.
     */
    private static final String FRONTEND_DEV_ORIGIN = "http://localhost:5173";

    /**
     * Creates the application clock bean using the UTC timezone.
     *
     * @return shared UTC clock instance
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Registers the global CORS policy used by the application.
     *
     * <p>This configuration allows the local development frontend to consume the
     * RootCause API from a separate origin while keeping the policy explicit and
     * centralized.</p>
     *
     * <p>The configuration applies to all API endpoints under {@code /api/v1/**} and
     * currently allows the HTTP methods required by the existing backend and the
     * upcoming frontend integration.</p>
     *
     * @return web MVC configurer that registers the global CORS mappings
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {

            /**
             * Adds the global CORS mappings for the RootCause API.
             *
             * @param registry Spring MVC CORS registry
             */
            @Override
            public void addCorsMappings(final CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                        .allowedOrigins(FRONTEND_DEV_ORIGIN)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}