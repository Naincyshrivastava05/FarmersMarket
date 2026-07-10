package com.cropdeal.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * No @EnableFeignClients here -- the gateway never makes
 * Feign calls, it only forwards raw HTTP requests.
 * No @EnableRabbitMQ -- gateway has no messaging.
 * No JPA -- gateway has no database.
 * It's the simplest main class in the whole project.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}