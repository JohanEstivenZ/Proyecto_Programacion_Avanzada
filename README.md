# TaskFlow API — Proyecto de Ejemplo

**Sistema de Gestion de Tareas de Equipo** — Spring Boot 3.3 + JPA + JWT + Spring AI

Proyecto de referencia para estudiantes del Programa de Ingenieria de Sistemas, Universidad del Quindio. Cubre las tecnologias y patrones del stack de la materia con explicaciones en cada archivo.

---

## Inicio rapido

```bash
# 1. Clonar
git clone https://github.com/josealfredore2604/Proyecto-Base-Springboot.git
cd taskflow-api

# 2. Crear archivo de variables de entorno
cp .env.example .env

# 3. Ejecutar (usa H2 en memoria, no necesita instalar nada)
./gradlew bootRun
```

**URL base:** `http://localhost:8080/api/v1`
**Swagger:** `http://localhost:8080/api/v1/swagger-ui.html`
**H2 Console:** `http://localhost:8080/api/v1/h2-console` (JDBC URL: `jdbc:h2:mem:taskflowdb`)

**Usuarios precargados:**

| Rol | Email | Password |
|-----|-------|----------|
| ADMIN | admin@taskflow.dev | admin123 |
| TEAM_LEAD | pedro@taskflow.dev | lead123 |
| DEVELOPER | ana@taskflow.dev | dev123 |

---

## Estructura del proyecto

```
taskflow-api/
├── .env.example               ← Plantilla de variables de entorno
├── .gitignore                 ← Archivos excluidos de Git
├── build.gradle               ← Dependencias y plugins (ver comentarios dentro)
├── settings.gradle            ← Nombre del proyecto Gradle
├── README.md                  ← Este archivo
├── postman/
│   └── TaskFlow_API.postman_collection.json
└── src/
    ├── main/
    │   ├── java/co/edu/uniquindio/taskflow/
    │   │   ├── TaskFlowApplication.java    ← Punto de entrada (@SpringBootApplication)
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java    ← Seed data (CommandLineRunner)
    │   │   │   ├── IAServiceConfig.java    ← Factory condicional de IA
    │   │   │   ├── JwtAuthenticationFilter.java ← Filtro HTTP para JWT
    │   │   │   ├── JwtUtils.java           ← Generar/validar tokens JWT
    │   │   │   ├── OpenApiConfig.java      ← Swagger UI
    │   │   │   └── SecurityConfig.java     ← Reglas de seguridad HTTP
    │   │   ├── controller/
    │   │   │   ├── AuthController.java     ← POST /auth/login, /auth/registro
    │   │   │   ├── IAController.java       ← POST /ia/sugerencias, /ia/resumen
    │   │   │   └── TareaController.java    ← CRUD + transiciones
    │   │   ├── domain/
    │   │   │   ├── enums/                  ← EstadoTarea, Categoria, Prioridad, Rol
    │   │   │   └── model/
    │   │   │       ├── Tarea.java          ← Aggregate Root (logica de negocio)
    │   │   │       ├── HistorialTarea.java ← Registro auditable por accion
    │   │   │       └── Usuario.java        ← Usuarios del sistema
    │   │   ├── dto/
    │   │   │   ├── request/                ← Lo que el cliente ENVIA
    │   │   │   └── response/               ← Lo que el cliente RECIBE
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java ← Centraliza errores HTTP
    │   │   │   ├── RecursoNoEncontradoException.java
    │   │   │   └── ReglaNegocioException.java
    │   │   ├── repository/                 ← Interfaces JPA (Spring Data genera implementacion)
    │   │   └── service/
    │   │       ├── impl/
    │   │       │   ├── AuthServiceImpl.java
    │   │       │   ├── IAServiceFallbackImpl.java  ← Reglas (sin internet)
    │   │       │   ├── IAServiceOpenAIImpl.java    ← Spring AI + OpenAI
    │   │       │   └── TareaServiceImpl.java       ← Logica principal
    │   │       ├── AuthService.java
    │   │       ├── IAService.java
    │   │       └── TareaService.java
    │   └── resources/
    │       ├── application.properties       ← Config base
    │       ├── application-dev.properties   ← H2 en memoria
    │       └── application-prod.properties  ← MariaDB
    └── test/
        └── java/co/edu/uniquindio/taskflow/
            ├── controller/TareaControllerTest.java  ← @WebMvcTest
            ├── repository/
            │   ├── TareaRepositoryTest.java         ← @DataJpaTest
            │   └── UsuarioRepositoryTest.java
            └── service/TareaServiceTest.java        ← Mockito
```

---

## Conceptos clave explicados

### 1. Que es Spring Boot

Spring Boot es un framework que simplifica la creacion de aplicaciones Spring. Sin Spring Boot, tendrias que configurar manualmente el servidor web, la conexion a BD, la serializacion JSON, la seguridad, etc. Spring Boot lo hace automaticamente basandose en las dependencias que declares en `build.gradle`.

### 2. Que es Gradle

Gradle es el sistema de construccion (build tool). Lee `build.gradle`, descarga las dependencias de Maven Central, compila el codigo, ejecuta los tests y genera el JAR desplegable. Es la alternativa moderna a Maven (que usa XML).

### 3. Que es JPA e Hibernate

**JPA** (Jakarta Persistence API) es una especificacion (interfaz) que define como mapear clases Java a tablas SQL. **Hibernate** es la implementacion mas usada de JPA. Cuando escribes `@Entity` en una clase, Hibernate la mapea a una tabla. Cuando escribes `repository.save(entidad)`, Hibernate genera el `INSERT INTO` o `UPDATE` correspondiente.

### 4. Que es Spring Data JPA

Es una capa sobre JPA/Hibernate que genera implementaciones automaticas de repositorios. Solo declaras una interfaz (`extends JpaRepository`) y Spring crea la clase con los metodos CRUD en tiempo de ejecucion. Incluso genera queries a partir del nombre del metodo: `findByEmail(email)` se convierte en `SELECT * FROM usuarios WHERE email = ?`.

### 5. Que es un DTO

Data Transfer Object. Es una clase simple (sin logica) que define la forma exacta de los datos que entran o salen de la API. Separa lo que el cliente ve de la estructura interna de la entidad. Ejemplo: el cliente no necesita ver la version de concurrencia ni el hash del password.

### 6. Que es JWT

JSON Web Token. Es un string codificado en Base64 que contiene datos del usuario (ID, rol, expiracion) firmado con una clave secreta. El cliente lo envia en cada peticion (`Authorization: Bearer <token>`) y el servidor lo valida sin necesidad de consultar la BD. Es stateless: no hay sesiones en el servidor.

### 7. Que es la concurrencia optimista

Tecnica para evitar que dos usuarios sobrescriban los cambios del otro. Cada entidad tiene un campo `version`. Al actualizar, se verifica que la version no haya cambiado. Si cambio, significa que alguien mas la modifico y se rechaza la operacion con 409 Conflict.

### 8. Que es DDD (Domain-Driven Design)

Patron de diseno donde la logica de negocio vive en las entidades del dominio, no en los servicios. En este proyecto, la clase `Tarea` contiene los metodos `iniciar()`, `completar()`, `archivar()`, con sus validaciones internas. El servicio solo orquesta (buscar, validar version, guardar).

### 9. Que es Spring AI

Modulo oficial de Spring para integrar modelos de lenguaje (LLM). Proporciona un `ChatClient` que abstrae la comunicacion con OpenAI, Anthropic, Ollama, etc. En este proyecto se usa para sugerir clasificaciones y generar resumenes de tareas.

### 10. Que es el patron de perfiles

Spring Boot permite tener multiples archivos de configuracion:
- `application.properties` → siempre se carga.
- `application-dev.properties` → se carga encima si el perfil es "dev".
- `application-prod.properties` → se carga encima si el perfil es "prod".

Esto permite usar H2 en desarrollo y MariaDB en produccion sin cambiar codigo.

---

## Guia de anotaciones

### Spring Core
| Anotacion | Que hace |
|-----------|----------|
| `@SpringBootApplication` | Punto de entrada. Combina @Configuration + @EnableAutoConfiguration + @ComponentScan |
| `@Component` | Registra la clase como bean en el contenedor de Spring |
| `@Service` | Igual que @Component, pero semanticamente indica capa de negocio |
| `@Repository` | Igual que @Component, pero indica capa de persistencia. Traduce excepciones SQL |
| `@Configuration` | La clase contiene metodos @Bean que definen beans |
| `@Bean` | El retorno del metodo se registra como bean singleton en Spring |
| `@Value("${prop}")` | Inyecta el valor de una propiedad de application.properties o .env |
| `@Profile("!test")` | Solo crea el bean si el perfil activo NO es "test" |
| `@PostConstruct` | Se ejecuta una vez despues de que Spring inyecta las dependencias |
| `@RequiredArgsConstructor` | Lombok genera constructor con campos final → inyeccion por constructor |

### Spring Web
| Anotacion | Que hace |
|-----------|----------|
| `@RestController` | @Controller + @ResponseBody. Retorna JSON directamente |
| `@RequestMapping("/ruta")` | Prefijo de URL para todo el controller |
| `@GetMapping` / `@PostMapping` / `@PatchMapping` | Mapea metodo HTTP a metodo Java |
| `@PathVariable` | Extrae valor de la URL: `/tareas/{id}` |
| `@RequestParam` | Extrae query param: `?estado=PENDIENTE` |
| `@RequestBody` | Deserializa el body JSON a un objeto Java |
| `@Valid` | Activa la validacion del DTO antes de ejecutar el metodo |
| `@RestControllerAdvice` | Manejador global de excepciones para todos los controllers |
| `@ExceptionHandler` | Captura un tipo de excepcion y define la respuesta HTTP |

### JPA / Hibernate
| Anotacion | Que hace |
|-----------|----------|
| `@Entity` | Mapea la clase a una tabla SQL |
| `@Table(name = "x")` | Nombre explicito de la tabla |
| `@Id` | Clave primaria |
| `@UuidGenerator` | Genera UUID automaticamente al persistir |
| `@Column(...)` | Configura nombre, nullable, length, unique de la columna |
| `@Enumerated(EnumType.STRING)` | Guarda el enum como texto, no como numero |
| `@ManyToOne(fetch = LAZY)` | Relacion N:1. LAZY = no carga hasta que se accede |
| `@OneToMany(cascade = ALL)` | Relacion 1:N. CASCADE = guardar padre guarda hijos |
| `@JoinColumn` | Define la columna de foreign key |
| `@Version` | Concurrencia optimista. Se incrementa en cada UPDATE |
| `@PrePersist` | Callback antes del INSERT |
| `@Index` | Crea indice en la BD para acelerar consultas |
| `@Query` | Define query JPQL personalizada |
| `@Transactional` | Ejecuta el metodo dentro de una transaccion |
| `@Transactional(readOnly = true)` | Transaccion de solo lectura (mejor rendimiento) |

### Validacion
| Anotacion | Que hace |
|-----------|----------|
| `@NotNull` | No puede ser null |
| `@NotBlank` | No puede ser null, vacio ni solo espacios |
| `@Size(min, max)` | Longitud del String |
| `@Email` | Formato de email valido |

### Lombok
| Anotacion | Que hace |
|-----------|----------|
| `@Getter` / `@Setter` | Genera getters/setters |
| `@Builder` | Patron Builder: `Obj.builder().campo(val).build()` |
| `@NoArgsConstructor` | Constructor vacio (requerido por JPA) |
| `@AllArgsConstructor` | Constructor con todos los campos (requerido por @Builder) |
| `@Slf4j` | Genera campo `log` para logging |
| `@Builder.Default` | Valor por defecto en el Builder |

### Spring Security
| Anotacion | Que hace |
|-----------|----------|
| `@EnableWebSecurity` | Activa seguridad web |
| `@EnableMethodSecurity` | Permite @PreAuthorize en metodos |

### Swagger
| Anotacion | Que hace |
|-----------|----------|
| `@Tag` | Agrupa endpoints en Swagger UI |
| `@Operation` | Documenta un endpoint |

---

## Endpoints de la API

### Autenticacion (publicos)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | `/auth/login` | Login → retorna JWT |
| POST | `/auth/registro` | Registro → retorna JWT |

### Tareas (requieren JWT)
| Metodo | Ruta | Rol minimo |
|--------|------|-----------|
| POST | `/tareas` | Cualquiera |
| GET | `/tareas` | Cualquiera |
| GET | `/tareas/{id}` | Cualquiera |
| PATCH | `/tareas/{id}/clasificar` | TEAM_LEAD |
| PATCH | `/tareas/{id}/asignar` | TEAM_LEAD |
| PATCH | `/tareas/{id}/iniciar` | Cualquiera |
| PATCH | `/tareas/{id}/revision` | Cualquiera |
| PATCH | `/tareas/{id}/completar` | TEAM_LEAD |
| PATCH | `/tareas/{id}/archivar` | TEAM_LEAD |

### IA (requieren JWT)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | `/ia/sugerencias/clasificacion` | Sugiere categoria + prioridad |
| POST | `/ia/resumen` | Genera resumen del historial |

---

## Ejemplos con cURL

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@taskflow.dev","password":"dev123"}'

# Guardar el token
TOKEN="eyJhbG..."

# Crear tarea
curl -X POST http://localhost:8080/api/v1/tareas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"titulo":"Corregir bug login","descripcion":"El boton no responde en Safari iOS 17"}'

# Listar con filtros
curl "http://localhost:8080/api/v1/tareas?estado=PENDIENTE&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Sugerencia de IA
curl -X POST http://localhost:8080/api/v1/ia/sugerencias/clasificacion \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"descripcion":"Bug urgente en produccion que crashea la app"}'
```

---

## Coleccion Postman

Importar `postman/TaskFlow_API.postman_collection.json` en Postman. Las variables `{{token}}`, `{{tareaId}}` y `{{tareaVersion}}` se actualizan automaticamente al ejecutar los requests en orden.

---

## Tests

```bash
./gradlew test
```

**Tipos de tests incluidos:**

| Tipo | Anotacion | Que carga | Velocidad |
|------|-----------|-----------|-----------|
| Unitario | `@ExtendWith(MockitoExtension.class)` | Nada (solo mocks) | Muy rapido |
| Controller | `@WebMvcTest` | Solo capa web | Rapido |
| Repository | `@DataJpaTest` | Solo JPA + H2 | Rapido |
| Integracion | `@SpringBootTest` | Todo el contexto | Lento |

---

## Activar OpenAI

1. Obtener API key en https://platform.openai.com/api-keys
2. Editar `.env`:
   ```
   OPENAI_API_KEY=sk-tu-clave-real
   IA_PROVIDER=openai
   ```
3. Reiniciar la app. El log mostrara "IA Provider: OpenAI".

Sin API key, el sistema funciona con el fallback de reglas.

---

## Produccion con MariaDB

```sql
CREATE DATABASE IF NOT EXISTS taskflow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```
# .env
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mariadb://localhost:3306/taskflow_db
DB_USERNAME=root
DB_PASSWORD=tu_password
```

```bash
./gradlew bootRun
```
