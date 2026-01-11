package com.dam.accesodatos.mongodb.springdata;

import com.dam.accesodatos.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORIO SPRING DATA MONGODB
 * ================================
 * Interface que extiende MongoRepository para operaciones CRUD automáticas.
 *
 * COMPARACIÓN CON SPRING DATA JPA:
 * ================================
 * MongoDB:                                    | JPA:
 * ------------------------------------------- | -------------------------------------------
 * extends MongoRepository<User, String>       | extends JpaRepository<User, Long>
 * String = tipo del ID (ObjectId como String) | Long = tipo del ID (autoincrement)
 * Query methods: findByDepartment(String)     | Query methods: findByDepartment(String)
 * countByDepartment(String)                   | countByDepartment(String)
 *
 * MÉTODOS AUTOMÁTICOS (HEREDADOS DE MongoRepository):
 * --------------------------------------------------
 * - save(User user)           → INSERT o UPDATE automático
 * - findById(String id)       → SELECT WHERE _id = ?
 * - findAll()                 → SELECT * FROM users
 * - delete(User user)         → DELETE FROM users WHERE _id = ?
 * - count()                   → SELECT COUNT(*) FROM users
 * - existsById(String id)     → SELECT COUNT(*) WHERE _id = ?
 *
 * QUERY METHODS (DERIVADOS DEL NOMBRE):
 * -------------------------------------
 * Spring Data parsea el nombre del método y genera la query automáticamente.
 *
 * Ejemplos MongoDB:
 * - findByDepartment(String dept)              → db.users.find({ department: "IT" })
 * - findByNameContainingIgnoreCase(String n)   → db.users.find({ name: /nombre/i })
 * - countByDepartment(String dept)             → db.users.countDocuments({ department: "IT" })
 * - findByActive(Boolean active)               → db.users.find({ active: true })
 *
 * Equivalente SQL/JPA:
 * - findByDepartment(String dept)              → SELECT * FROM users WHERE department = ?
 * - findByNameContainingIgnoreCase(String n)   → SELECT * FROM users WHERE UPPER(name) LIKE UPPER('%?%')
 * - countByDepartment(String dept)             → SELECT COUNT(*) FROM users WHERE department = ?
 *
 * VENTAJAS SPRING DATA (MongoDB y JPA):
 * - NO escribes código, solo defines la interface
 * - Query methods son type-safe (compile-time validation)
 * - Reduce boilerplate en un 90% vs API nativa
 *
 * DESVENTAJAS:
 * - "Magia" que oculta cómo funcionan las queries
 * - Queries complejas requieren @Query o MongoTemplate/EntityManager
 * - Menor control sobre optimización
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // Equivalente JPA: public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * QUERY METHOD: Buscar por departamento
     * =====================================
     * Spring Data MongoDB genera automáticamente:
     * db.users.find({ department: "IT" })
     *
     * Equivalente JPA generaría:
     * SELECT u FROM User u WHERE u.department = :department
     */
    List<User> findByDepartment(String department);
    
    /**
     * QUERY METHOD: Contar por departamento
     * =====================================
     * Spring Data MongoDB genera:
     * db.users.countDocuments({ department: "IT" })
     *
     * Equivalente JPA:
     * SELECT COUNT(u) FROM User u WHERE u.department = :department
     */
    long countByDepartment(String department);
    
    /**
     * QUERY METHOD: Buscar por estado activo/inactivo
     * ===============================================
     * MongoDB: db.users.find({ active: true })
     * SQL: SELECT * FROM users WHERE active = ?
     */
    List<User> findByActive(Boolean active);
    
    /**
     * QUERY METHOD: Búsqueda parcial por nombre (case-insensitive)
     * ============================================================
     * MongoDB: db.users.find({ name: /busqueda/i })
     * SQL: SELECT * FROM users WHERE UPPER(name) LIKE UPPER('%busqueda%')
     *
     * Palabras clave de Spring Data:
     * - Containing: búsqueda parcial (LIKE %valor%)
     * - IgnoreCase: case-insensitive
     * - StartingWith: LIKE valor%
     * - EndingWith: LIKE %valor
     * - Between, LessThan, GreaterThan: comparaciones
     */
    List<User> findByNameContainingIgnoreCase(String name);
}
