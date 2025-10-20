# Cine Pass Management API
**CinePass** es una aplicación web desarrollada con Spring Boot y arquitectura MVC, conectada a una base de datos mediante Spring Data JPA, diseñada para gestionar la venta de boletos de cine online de manera agil y segura.

---
## Tecnologias utilizadas
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Lombok
- MySQL
- Hibernate
- Maven
- PostMan (Para pruebas)

---
## Arquitectura
La aplicacion siguen el patrón **MVC** (Modelo - Vista - Controlador) con una estructura modular y escalable:

```
  src
└── main
    ├── java
    │   └── com.api.boletería
    │       ├── config
    │       ├── controller
    │       ├── dto
    │    	 	 ├──  	detail
    │    	 	 ├──  	list
    │    	 	 ├──  	request
    │       ├── exception
    │       ├── model
    │    	 	 ├── enums  	
    │       ├── repository
    │       ├── service
    │       ├── validators
    └── resources
        └── application.properties
```
---
## Seguridad 
La seguridad de la API se configuró usando `SecurityFilterChain` con JWT y manejo de sesión stateless. La autenticación se maneja mediante **HTTP Basic Auth** usando `Spring Security`. Algunas rutas están protegidas y requieren estar autenticado para acceder.

---

## Funciones Iniciales
 - Registro y autenticación de usuarios (cliente y administrador).
 - Gestión CRUD de películas, funciones, salas, usuarios y entradas.
 - Asignación de funciones a salas en fechas y horarios específicos.
 - Compra de entradas por parte de los clientes, con validación de disponibilidad.
 - Visualización de cartelera y funciones por parte de los usuarios.
 - Control de acceso según roles.

---

## Como correr el proyecto
 1. Clonar el repositorio:   
    ```bash
    git clone https://github.com/ilgazzolo/CinePass-Management.git
    cd CinePass-Management  
    ```
 2. Configurar la base de datos en src/main/application.properties:
    ```bash
    spring.datasource.url=jdbc:mysql://localhost:3306/cinepass_db
    spring.datasource.username=tu_usuario
    spring.datasource.password=tu_contraseña
    spring.jpa.hibernate.ddl-auto=create
    ```
 3. Ejecuta la aplicación:
    ```bash
    ./mvnw spring-boot:run

---

## Licencia
Este proyecto es de uso académico. Todos los derechos reservados.

Luciano Gazzolo  
Sebastian Aguilera  
Tomás Jopia  
Tomás Constantini  
Nahuel Ramirez  

---

**Universidad Tecnologica Nacional**

---
