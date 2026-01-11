package com.dam.accesodatos.model;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ENTIDAD USER PARA MONGODB
 * ========================
 * Esta clase representa un documento de la colección "users" en MongoDB.
 * 
 * COMPARACIÓN CON JPA/HIBERNATE (SQL):
 * ------------------------------------
 * MongoDB (NoSQL)                  | JPA/Hibernate (SQL)
 * -------------------------------- | ----------------------------------
 * @Document(collection = "users")  | @Entity @Table(name = "users")
 * Mapea a colección de documentos  | Mapea a tabla relacional
 * Documentos JSON/BSON flexibles   | Filas con esquema fijo
 * Sin esquema predefinido          | Esquema definido por DDL (CREATE TABLE)
 * 
 * VENTAJAS MongoDB:
 * - Esquema flexible: puedes añadir campos sin alterar la "tabla"
 * - Documentos embebidos: no necesitas JOINs para relaciones 1:N simples
 * - Escalabilidad horizontal nativa
 * 
 * VENTAJAS JPA:
 * - Integridad referencial con claves foráneas
 * - Transacciones ACID complejas
 * - JOINs eficientes para consultas relacionales
 */
@Document(collection = "users")  // Equivalente a @Entity + @Table(name = "users") en JPA
public class User {

    /**
     * CAMPO ID (CLAVE PRIMARIA)
     * =========================
     * MongoDB:                 | JPA/Hibernate:
     * ------------------------ | ----------------------------
     * @Id                      | @Id
     * Tipo: String             | Tipo: Long (normalmente)
     * Valor: ObjectId (24 hex) | Valor: Long autogenerado
     * Ejemplo: "507f1f77..."   | Ejemplo: 1, 2, 3...
     * Generado por MongoDB     | @GeneratedValue(strategy = GenerationType.IDENTITY)
     * 
     * DIFERENCIA CLAVE:
     * - MongoDB genera ObjectIds únicos globalmente (sin secuencias de BD)
     * - JPA usa secuencias o auto-increment de la base de datos relacional
     */
    @Id
    private String id;  // En JPA sería: @Id @GeneratedValue private Long id;

    /**
     * CAMPO NAME CON ÍNDICE
     * =====================
     * @Indexed: Crea un índice en MongoDB para búsquedas rápidas por nombre
     * 
     * Equivalente SQL:
     * CREATE INDEX idx_name ON users(name);
     * 
     * Sin índice: MongoDB escanea TODOS los documentos (O(n))
     * Con índice: Búsqueda en tiempo logarítmico (O(log n))
     * 
     * En JPA sería:
     * @Column(name = "name", nullable = false, length = 50)
     * @Index // en @Table(indexes = @Index(columnList = "name"))
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Indexed  // Crea índice para búsquedas rápidas
    private String name;

    /**
     * CAMPO EMAIL CON ÍNDICE ÚNICO
     * ============================
     * @Indexed(unique = true): Garantiza que no haya emails duplicados
     * 
     * Equivalente SQL:
     * ALTER TABLE users ADD CONSTRAINT uk_email UNIQUE (email);
     * 
     * MongoDB lanzará excepción E11000 (duplicate key error) si intentas insertar email duplicado
     * 
     * En JPA sería:
     * @Column(name = "email", unique = true, nullable = false)
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Indexed(unique = true)  // Garantiza unicidad, como UNIQUE constraint en SQL
    private String email;

    /**
     * CAMPO DEPARTMENT CON ÍNDICE
     * ===========================
     * Este campo tendrá índice porque se usará frecuentemente en filtros:
     * - findByDepartment(String department)
     * - Consultas de estadísticas por departamento
     * 
     * En aplicaciones reales, "department" podría ser:
     * - MongoDB: String simple (como aquí) o referencia a ObjectId de colección "departments"
     * - JPA: @ManyToOne Department con clave foránea
     */
    @NotBlank(message = "El departamento es obligatorio")
    @Indexed  // Optimiza consultas como: db.users.find({department: "IT"})
    private String department;

    /**
     * CAMPO ROLE (SIN ÍNDICE)
     * =======================
     * No tiene @Indexed porque no se usa frecuentemente en búsquedas
     * Añadir índices innecesarios:
     * - Ralentiza INSERT/UPDATE/DELETE
     * - Consume espacio en disco
     * 
     * Regla de oro: Solo indexa campos que uses en WHERE, ORDER BY o JOIN
     */
    @NotBlank(message = "El rol es obligatorio")
    private String role;

    /**
     * CAMPO ACTIVE CON MAPEO EXPLÍCITO
     * =================================
     * @Field("active"): Especifica el nombre del campo en el documento MongoDB
     * 
     * Si no usas @Field, Spring Data MongoDB usa el nombre del atributo Java.
     * Es útil cuando:
     * - El nombre en BD difiere del nombre en Java
     * - Tienes legacy data con nombres específicos
     * 
     * En JPA sería:
     * @Column(name = "active")
     * private Boolean active;
     */
    @Field("active")
    private Boolean active;

    /**
     * AUDITORÍA: FECHA DE CREACIÓN
     * ============================
     * @CreatedDate: Spring Data MongoDB asigna automáticamente la fecha de creación
     * 
     * Requiere @EnableMongoAuditing en configuración.
     * 
     * Equivalente en JPA:
     * @CreationTimestamp (Hibernate)
     * o @PrePersist con EntityListener
     * 
     * SQL alternativo:
     * CREATE TABLE users (
     *   ...
     *   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * );
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * AUDITORÍA: FECHA DE ÚLTIMA MODIFICACIÓN
     * =======================================
     * @LastModifiedDate: Spring Data MongoDB actualiza automáticamente este campo en cada save()
     * 
     * Equivalente en JPA:
     * @UpdateTimestamp (Hibernate)
     * 
     * SQL alternativo:
     * CREATE TRIGGER update_timestamp BEFORE UPDATE ON users
     * FOR EACH ROW SET NEW.updated_at = CURRENT_TIMESTAMP;
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * CONSTRUCTOR POR DEFECTO
     * =======================
     * Inicializa valores por defecto:
     * - active = true (nuevo usuario está activo)
     * - timestamps actuales
     * 
     * COMPARACIÓN CON JPA:
     * En JPA también necesitas constructor sin argumentos para que Hibernate
     * pueda instanciar la entidad desde ResultSet.
     * 
     * En JPA los timestamps se manejarían con:
     * @PrePersist void onCreate() { this.createdAt = LocalDateTime.now(); }
     */
    public User() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * CONSTRUCTOR PARA CREACIÓN DE USUARIOS
     * =====================================
     * No incluye ID porque MongoDB lo genera automáticamente.
     * 
     * COMPARACIÓN CON JPA:
     * En JPA también excluirías el ID del constructor porque se autogenera:
     * public User(String name, String email...) { ... }
     * // ID se genera al hacer: entityManager.persist(user);
     */
    public User(String name, String email, String department, String role) {
        this();
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
    }

    /**
     * CONSTRUCTOR CON ID (PARA TESTS O MAPEO MANUAL)
     * ==============================================
     * Útil cuando mapeas manualmente desde Document o para tests.
     */
    public User(String id, String name, String email, String department, String role) {
        this(name, email, department, role);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /**
     * SETTER CON AUDITORÍA MANUAL
     * ===========================
     * Actualiza updatedAt cada vez que cambia el nombre.
     * 
     * NOTA IMPORTANTE:
     * - Con @LastModifiedDate este update manual NO es necesario
     * - Spring Data MongoDB actualiza updatedAt automáticamente en save()
     * - Este patrón es útil si NO usas @EnableMongoAuditing
     * 
     * COMPARACIÓN JDBC:
     * En JDBC tendrías que hacer:
     * UPDATE users SET name = ?, updated_at = NOW() WHERE id = ?
     */
    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
        this.updatedAt = LocalDateTime.now();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * MÉTODO EQUALS - IGUALDAD DE ENTIDADES
     * =====================================
     * Dos usuarios son iguales si tienen mismo ID y mismo email.
     * 
     * COMPARACIÓN CON JPA:
     * - JPA recomienda usar solo el ID para equals/hashCode
     * - Aquí usamos ID + email para mayor seguridad en caso de usuarios sin ID (nuevos)
     * 
     * IMPORTANTE:
     * - Nunca uses todos los campos en equals() (puede causar problemas con lazy loading en JPA)
     * - Email es único, por eso es seguro incluirlo
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(email, user.email);
    }

    /**
     * MÉTODO HASHCODE - PARA COLECCIONES (SET, MAP)
     * =============================================
     * Debe ser consistente con equals():
     * - Si obj1.equals(obj2) → obj1.hashCode() == obj2.hashCode()
     * 
     * Usamos los mismos campos que equals() (id + email)
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
