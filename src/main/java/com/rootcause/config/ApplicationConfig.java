package com.rootcause.config;

import org.springframework.context.annotation.*;

import java.time.Clock;

@Configuration
public class ApplicationConfig {
    /*
    * Esta clase se encarga de configurar los beans necesarios para la aplicación. En este caso, se define un bean de tipo Clock que se utiliza para obtener la hora actual en formato UTC.
    * Esto es útil para garantizar que todas las operaciones relacionadas con el tiempo sean consistentes y
    * no dependan de la zona horaria del servidor donde se ejecute la aplicación.
    * */

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

}
