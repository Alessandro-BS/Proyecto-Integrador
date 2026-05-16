# 🚀 Guía de Despliegue y Ejecución — SISOL Salud

Este documento detalla los pasos necesarios para configurar, ejecutar y desplegar el backend del Sistema Inteligente de Turnos de SISOL Salud.

## 🛠️ Tecnologías Utilizadas
- **Java 17** (Amazon Corretto o similar)
- **Spring Boot 3.5.x**
- **MySQL 8.0**
- **Maven**
- **Hibernate Envers** (Auditoría)
- **Spring Security + JWT**
- **Logback** (Gestión de logs)

---

## 📋 Requisitos Previos
1. **Java JDK 17** instalado.
2. **MySQL Server 8.0** corriendo localmente o en un servidor.
3. **Maven** instalado (o usar el wrapper `./mvnw` incluido).
4. Una cuenta de correo (ej. Gmail) para el envío de notificaciones (opcional, configurar en `application.yml`).

---

## ⚙️ Configuración de la Base de Datos
1. Crear la base de datos en MySQL:
   ```sql
   CREATE DATABASE sisol_salud_db;
   ```
2. Configurar las credenciales en `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/sisol_salud_db
       username: tu_usuario
       password: tu_password
   ```

---

## 🏃 Ejecución Local
1. Clonar el repositorio.
2. Ejecutar el comando:
   ```bash
   ./mvnw spring-boot:run
   ```
3. El servidor iniciará en `http://localhost:8080`.

---

## 🧪 Ejecución de Pruebas
Para ejecutar la suite completa de pruebas unitarias, de integración y de repositorio:
```bash
./mvnw test
```
*Los tests utilizan una base de datos **H2 en memoria**, por lo que no afectan tu base de datos de desarrollo.*

---

## 📦 Generación de Artefacto (JAR)
Para generar el archivo ejecutable para producción:
```bash
./mvnw clean package -DskipTests
```
El archivo se generará en la carpeta `target/salud-0.0.1-SNAPSHOT.jar`.

---

## 🛡️ Monitoreo y Auditoría
- **Health Check:** `GET http://localhost:8080/actuator/health`
- **Info:** `GET http://localhost:8080/actuator/info`
- **Logs:** Los logs se almacenan en la carpeta `/logs` de la raíz del proyecto, con rotación diaria.
- **Auditoría:** Todas las tablas principales tienen su correspondiente tabla `_AUD` para trazabilidad de cambios.

---

## 📖 Documentación de la API (Swagger)
Una vez iniciada la aplicación, accede a:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs:** `http://localhost:8080/api-docs`
