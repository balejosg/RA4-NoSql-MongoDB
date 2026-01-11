package com.dam.accesodatos.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * CONFIGURACIÓN DE MONGODB
 * ========================
 * Esta clase configura la conexión a MongoDB y habilita Spring Data MongoDB.
 *
 * COMPARACIÓN CON JPA/HIBERNATE:
 * ===============================
 * MongoDB                                    | JPA/Hibernate
 * ------------------------------------------ | ------------------------------------------
 * MongoConfig extends                        | @Configuration + EntityManagerFactory
 *   AbstractMongoClientConfiguration         |
 * @EnableMongoRepositories                   | @EnableJpaRepositories
 * MongoClient (conexión nativa)              | DataSource (JDBC)
 * MongoTemplate (operaciones de bajo nivel)  | EntityManager / JdbcTemplate
 * No necesita DDL (sin esquema fijo)         | Hibernate auto-ddl: create/update/validate
 *
 * VENTAJAS MongoDB:
 * - No necesitas definir esquema (schema-less)
 * - Conexión más simple (solo host:port/database)
 * - No requiere pool de conexiones complejo
 *
 * VENTAJAS JPA:
 * - Control explícito de transacciones
 * - Validación de esquema en compile-time
 * - Soporte estándar para múltiples bases de datos
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.dam.accesodatos.mongodb.springdata")
// Equivalente JPA: @EnableJpaRepositories(basePackages = "com.dam.accesodatos.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {
    // Equivalente JPA: public class JpaConfig { ... DataSource, EntityManagerFactory ... }

    @Value("${spring.data.mongodb.database}")
    private String databaseName;  // En JPA sería el nombre en la URL JDBC

    @Value("${spring.data.mongodb.host}")
    private String host;  // localhost o IP del servidor MongoDB

    @Value("${spring.data.mongodb.port}")
    private int port;  // Por defecto 27017 para MongoDB

    /**
     * NOMBRE DE LA BASE DE DATOS
     * ==========================
     * Requerido por AbstractMongoClientConfiguration.
     * 
     * Equivalente JPA:
     * En JPA esto estaría en la URL JDBC:
     * jdbc:mysql://localhost:3306/nombre_base_datos
     */
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * CLIENTE MONGODB (CONEXIÓN NATIVA)
     * ==================================
     * MongoClient es la clase principal del driver nativo de MongoDB.
     * 
     * COMPARACIÓN CON JDBC:
     * MongoDB:                           | JDBC:
     * ---------------------------------- | -----------------------------------
     * MongoClient mongoClient =          | Connection conn = DriverManager
     *   MongoClients.create("mongodb://  |   .getConnection("jdbc:mysql://
     *   localhost:27017")                |   localhost:3306/db")
     * 
     * IMPORTANTE:
     * - MongoClient es thread-safe (puedes compartirlo entre threads)
     * - En JPA usarías DataSource con pool de conexiones (HikariCP)
     * - MongoDB maneja internamente el pooling de conexiones
     * 
     * Ejemplo connection string completa:
     * mongodb://username:password@localhost:27017/database?authSource=admin
     */
    @Override
    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format("mongodb://%s:%d", host, port);
        return MongoClients.create(connectionString);
        
        // Equivalente JDBC sería:
        // @Bean
        // public DataSource dataSource() {
        //     HikariConfig config = new HikariConfig();
        //     config.setJdbcUrl("jdbc:mysql://localhost:3306/db");
        //     config.setUsername("user");
        //     config.setPassword("pass");
        //     return new HikariDataSource(config);
        // }
    }

    /**
     * MONGOTEMPLATE (API DE BAJO NIVEL)
     * =================================
     * MongoTemplate proporciona operaciones de bajo nivel similares a JdbcTemplate.
     * 
     * COMPARACIÓN:
     * MongoDB                          | JPA/JDBC
     * -------------------------------- | ------------------------------------
     * MongoTemplate mongoTemplate      | JdbcTemplate jdbcTemplate
     * mongoTemplate.find(query, User)  | jdbcTemplate.query(sql, rowMapper)
     * mongoTemplate.insert(user)       | jdbcTemplate.update(insertSql, params)
     * mongoTemplate.aggregate(...)     | jdbcTemplate.query(complexSql, ...)
     * 
     * CUÁNDO USAR MongoTemplate:
     * - Queries complejas que no se pueden expresar con repository methods
     * - Aggregation pipelines
     * - Operaciones bulk (insertar/actualizar miles de documentos)
     * - Queries dinámicas con Criteria API
     * 
     * CUÁNDO USAR MongoRepository:
     * - Operaciones CRUD simples
     * - Query methods derivados (findByDepartment, etc.)
     * - Aplicaciones con lógica de negocio simple
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
        
        // Equivalente JPA:
        // @Bean
        // public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        //     return new JdbcTemplate(dataSource);
        // }
    }

    /**
     * BEAN AUXILIAR PARA INYECTAR NOMBRE DE BASE DE DATOS
     * ===================================================
     * Permite inyectar el nombre de la base de datos en servicios nativos.
     * Usado en NativeMongoUserServiceImpl.
     */
    @Bean
    public String mongoDatabaseName() {
        return databaseName;
    }
}
