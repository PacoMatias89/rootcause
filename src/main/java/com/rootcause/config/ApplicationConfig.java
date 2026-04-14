package com.rootcause.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Spring configuration class for application-wide infrastructure beans.
 *
 * <p>This configuration currently provides a shared {@link Clock} bean to centralize
 * time access across the application.</p>
 *
 * <p>Using a single clock bean improves consistency in time-based operations and avoids
 * coupling the code to the default system clock directly. It also makes testing easier,
 * because the clock can be replaced or controlled in test scenarios.</p>
 *
 * <p>The configured clock uses UTC so that persisted timestamps and time-based logic do not
 * depend on the server local timezone.</p>
 */
@Configuration
public class ApplicationConfig {

    /**
     * Creates the application clock bean using the UTC timezone.
     *
     * @return shared UTC clock instance
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

}