package com.dam.accesodatos.config;

import com.dam.accesodatos.model.User;
import com.dam.accesodatos.mongodb.springdata.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * INICIALIZADOR DE DATOS
 * ======================
 * Carga datos de prueba automáticamente al arrancar la aplicación.
 *
 * COMPARACIÓN CON SQL/JPA:
 * ========================
 * MongoDB (Spring Data):                  | SQL/JPA:
 * --------------------------------------- | ---------------------------------------
 * CommandLineRunner que usa repository    | data.sql script o @PostConstruct
 * repository.saveAll(users)               | INSERT INTO users VALUES (...), (...)
 * No necesita schema.sql                  | Requiere CREATE TABLE en schema.sql
 * Documentos se crean automáticamente     | Tablas deben existir previamente
 *
 * VENTAJAS MONGODB:
 * - No necesitas definir esquema (schema-less)
 * - Colección "users" se crea automáticamente al insertar
 * - Puedes añadir/quitar campos sin migración de BD
 * - Código Java en vez de SQL scripts
 *
 * VENTAJAS SQL:
 * - data.sql es estándar e independiente del código
 * - Validación de esquema previene errores de estructura
 * - Mejor para migrar datos entre entornos
 *
 * PATRÓN PEDAGÓGICO:
 * - Este inicializador carga 8 usuarios de ejemplo
 * - Distribuidos en varios departamentos para practicar queries
 * - Uno inactivo para demostrar filtros booleanos
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * COMMAND LINE RUNNER
     * ===================
     * Se ejecuta automáticamente después de que Spring Boot arranca.
     *
     * Equivalente JPA/SQL:
     * - Option 1: src/main/resources/data.sql con INSERT statements
     * - Option 2: @PostConstruct en @Component
     * - Option 3: Flyway o Liquibase para migraciones
     *
     * LÓGICA:
     * 1. Verifica si ya hay datos (repository.count() > 0)
     * 2. Si hay datos, omite inicialización (idempotencia)
     * 3. Si no hay datos, carga 8 usuarios de ejemplo
     */
    @Bean
    public CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            // Idempotencia: solo cargar si BD está vacía
            if (repository.count() > 0) {
                log.info("Base de datos ya contiene datos, omitiendo inicialización");
                return;
            }

            log.info("Inicializando base de datos con usuarios de prueba...");

            // Crear 8 usuarios de ejemplo para prácticas
            var users = Arrays.asList(
                    createUser("Juan Pérez", "juan.perez@empresa.com", "IT", "Developer",
                            LocalDateTime.of(2024, 1, 15, 9, 30)),
                    createUser("María García", "maria.garcia@empresa.com", "HR", "Manager",
                            LocalDateTime.of(2024, 1, 16, 10, 15)),
                    createUser("Carlos López", "carlos.lopez@empresa.com", "Finance", "Analyst",
                            LocalDateTime.of(2024, 1, 17, 11, 0)),
                    createUser("Ana Martínez", "ana.martinez@empresa.com", "IT", "Senior Developer",
                            LocalDateTime.of(2024, 1, 18, 8, 45)),
                    createUser("Luis Rodríguez", "luis.rodriguez@empresa.com", "Marketing", "Specialist",
                            LocalDateTime.of(2024, 1, 19, 13, 20)),
                    // Usuario inactivo para demostrar filtros booleanos
                    createUserInactive("Elena Fernández", "elena.fernandez@empresa.com", "IT", "DevOps",
                            LocalDateTime.of(2024, 1, 20, 9, 0)),
                    createUser("Pedro Sánchez", "pedro.sanchez@empresa.com", "Sales", "Representative",
                            LocalDateTime.of(2024, 1, 21, 10, 30)),
                    createUser("Laura González", "laura.gonzalez@empresa.com", "HR", "Recruiter",
                            LocalDateTime.of(2024, 1, 22, 14, 0))
            );

            // Insertar todos los usuarios en una operación batch
            repository.saveAll(users);
            // Equivalente SQL: INSERT INTO users (...) VALUES (...), (...), ...
            // Equivalente JPA: entityManager.persist() en loop o saveAll()

            log.info("✓ Base de datos inicializada con {} usuarios", users.size());
            log.info("  - IT: 3 usuarios (1 inactivo)");
            log.info("  - HR: 2 usuarios");
            log.info("  - Finance, Marketing, Sales: 1 usuario cada uno");
        };
    }

    /**
     * MÉTODO AUXILIAR: Crear usuario activo
     * =====================================
     */
    private User createUser(String name, String email, String department, String role, LocalDateTime createdAt) {
        User user = new User(name, email, department, role);
        user.setActive(true);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(createdAt);
        return user;
    }

    /**
     * MÉTODO AUXILIAR: Crear usuario inactivo
     * =======================================
     * Para demostrar filtros: findByActive(false)
     */
    private User createUserInactive(String name, String email, String department, String role, LocalDateTime createdAt) {
        User user = createUser(name, email, department, role, createdAt);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.of(2024, 2, 1, 10, 0));
        return user;
    }
}
