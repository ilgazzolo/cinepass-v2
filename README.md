# CineMass - API de Pagos (Spring Boot + Mercado Pago)

Backend del sistema **CinePass**, desarrollado en **Spring Boot** y arquitectura MVC con integración a **Mercado Pago Checkout Pro (Sandbox)**, conectada a una base de datos mediante Spring Data JPA, diseñada para gestionar la venta de boletos de cine online de manera agil y segura.
  
Permite generar preferencias de pago y probar transacciones reales desde el entorno local usando **Ngrok**.

---

## Tecnologias utilizadas
- Java 17+
- Spring Boot 3.x
- Spring Security
- Lombok
- MySQL
- Hibernate
- Maven
- Mercado Pago SDK Java
- **Ngrok** (para exponer el backend local)
- Postman (para pruebas de endpoints)

---

## Requisitos previos

Antes de ejecutar el proyecto, asegurate de tener instalado:

- [Java 17 o superior](https://adoptium.net/)
- [Maven](https://maven.apache.org/)
- [MySQL](https://www.mysql.com/)
- [Ngrok](https://ngrok.com/)
- [Postman](https://www.postman.com/)

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
    │       ├── mercadopago
    │    	 	 ├──  	controller
    │    	 	 ├──  	dto
    │    	 	 ├──  	service
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
git clone https://github.com/ilgazzolo/cinema-management-api.git
cd cinepass-v2  
```
 2. Configurar la base de datos en src/main/application.properties:
```bash
spring.datasource.url=jdbc:mysql://localhost:3306/cinepass_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=create
```
 3. Configura las credenciales de Mercado pago:
```bash
# En Windows (Powershell)
setx MP_ACCESS_TOKEN "TU_ACCESS_TOKEN"
``` 
 4. Guarda tu token de Ngrok:
```bash
ngrok config add-authtoken TU_TOKEN_DE_NGROK
```
 5. Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
```
---

## Configuración inicial
  1. Iniciar el backend
```bash
mvn spring-boot:run
```
  2. Iniciar Ngrok
```bash
ngrok http 8080
```
  Vas a obtener una salida como esta:
```bash
Forwarding  https://mi-tunel-ngrok.ngrok-free.dev -> http://localhost:8080
```
  Esa URL pública será la que uses en Mercado Pago y Postman.
  
  3. Actualizar las URLs de Mercado Pago, abri el archivo:
  ```bash
  src/main/java/com/api/boleteria/service/PaymentService.java
  ```
  Y reemplazá las rutas por las tuyas de ngrok:
  ```bash
PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
    .success("https://<TU_TUNEL>.ngrok-free.dev/success")
    .pending("https://<TU_TUNEL>.ngrok-free.dev/pending")
    .failure("https://<TU_TUNEL>.ngrok-free.dev/failure")
    .build();

PreferenceRequest preferenceRequest = PreferenceRequest.builder()
    .items(List.of(itemRequest))
    .backUrls(backUrls)
    .notificationUrl("https://<TU_TUNEL>.ngrok-free.dev/api/payments/notification")
    .autoReturn("approved")
    .build();
```
  El dominio de ngrok expira cada vez que se reinicia, por lo tanto, debes actualizar las URLs cada vez que lo vuelvan a ejecutar.

---

## Licencia
Este proyecto es de uso académico. Todos los derechos reservados.

Luciano Gazzolo  
Sebastian Aguilera  
Tomás Costantini  
Nahuel Ramirez  

---

**Universidad Tecnologica Nacional**

---
