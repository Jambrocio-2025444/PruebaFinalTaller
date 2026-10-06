# Sistema Backend de Biblioteca Universitaria (Microservicios)

Este proyecto implementa una arquitectura de microservicios para la gestión de una biblioteca universitaria, cumpliendo con los estándares de separación de responsabilidades y bases de datos por servicio.

## Arquitectura y Microservicios

El sistema está compuesto por 5 microservicios independientes:

1. **API Gateway (Puerto 8080):** Punto de entrada principal. Enruta las peticiones usando Spring Cloud Gateway.
2. **Auth Service (Puerto 8081):** Genera y emite tokens JWT. Se comunica con `user-service` para verificar contraseñas.
3. **User Service (Puerto 8082):** Administra los usuarios y sus estados. Base de datos: `kinal_users`.
4. **Book Service (Puerto 8083):** Administra el catálogo y stock. Base de datos: `kinal_books`.
5. **Loan Service (Puerto 8084):** Orquesta las reglas de negocio de los préstamos. Base de datos: `kinal_loans`.

## Tecnologías Utilizadas
* Java 21
* Spring Boot 3.2.x (Web, Data JPA, Security)
* Spring Cloud Gateway
* JWT (JSON Web Tokens) con JJWT
* Hibernate & MySQL
* Jakarta Validation
* Comunicación Interna: `RestClient`

## Bases de Datos
Implementamos "Database per Service" a nivel lógico. El sistema requiere un único servidor MySQL en el puerto 3306 (con usuario/password `root`/`root`), pero Hibernate creará 3 bases de datos independientes al iniciar:
* `kinal_users`
* `kinal_books`
* `kinal_loans`

## Seguridad (JWT) y Roles
Arquitectura *stateless* (sin sesiones en memoria). Auth Service genera un token HMAC-256 (Bearer) que expira en 10 horas. 
* **ADMIN:** Control total (gestión de libros, usuarios, devoluciones).
* **BIBLIOTECARIO:** Gestión operativa (salida y entrada de libros).
* **LECTOR:** Acceso a "Mis Préstamos" e historial.

## Reglas de Negocio Implementadas
1. Valida stockDisponible > 0 antes de prestar (usando Optimistic Locking `@Version` para concurrencia).
2. Lector máximo 3 préstamos activos simultáneos.
3. Plazo fijo de préstamo: 14 días.
4. Tarea programada en `loan-service` que marca automáticamente a estado `ATRASADO` a media noche.
5. Sanción automática: Si el lector intenta pedir un préstamo teniendo uno atrasado, su estado cambia a `SANCIONADO` mediante una llamada asíncrona al `user-service` y se bloquea la solicitud.

## Transacciones Distribuidas
Para los préstamos, implementamos un patrón Saga coreografiado simplificado:
1. `loan-service` solicita a `book-service` reducir el stock.
2. Si falla `book-service`, la operación aborta.
3. Si la base local de `loan-service` falla al persistir, ejecuta una llamada de compensación `aumentar-stock` al `book-service`.

## Ejecución
1. Enciende MySQL.
2. Levanta las aplicaciones de Spring Boot (puedes empezar por `user-service` y `book-service`).
3. El `user-service` creará automáticamente el usuario `admin@biblioteca.com` con clave `Admin123`.

## Pruebas
Se incluye el archivo `KinalLibrary_Postman_Collection.json` en este directorio para importar en Postman y probar los flujos directamente a través del API Gateway (`localhost:8080`).
