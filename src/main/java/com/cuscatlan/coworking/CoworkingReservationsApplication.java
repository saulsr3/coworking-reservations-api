package com.cuscatlan.coworking;

import com.cuscatlan.coworking.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableCaching
@EnableAsync
public class CoworkingReservationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoworkingReservationsApplication.class, args);
    }
}
