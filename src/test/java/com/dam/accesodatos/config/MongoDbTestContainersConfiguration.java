package com.dam.accesodatos.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuración de Testcontainers para MongoDB.
 * Inicia un contenedor Docker con MongoDB para los tests.
 * Funciona en cualquier máquina con Docker instalado.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MongoDbTestContainersConfiguration {

    private static final MongoDBContainer MONGODB_CONTAINER;

    static {
        MONGODB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:7.0.5"))
                .withReuse(true);
        MONGODB_CONTAINER.start();
    }

    @Bean
    public MongoDBContainer mongoDBContainer() {
        return MONGODB_CONTAINER;
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGODB_CONTAINER::getReplicaSetUrl);
    }
}
