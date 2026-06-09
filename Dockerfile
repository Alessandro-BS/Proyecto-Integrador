# 1. Fase de Construcción (Build)
FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copiar el wrapper de maven y el archivo pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper
RUN chmod +x mvnw

# Descargar las dependencias
# Esto se hace antes de copiar el código para cachear la capa de dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src src
RUN ./mvnw package -DskipTests

# 2. Fase de Ejecución (Run)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el archivo .jar compilado desde la fase de construcción
COPY --from=build /workspace/app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
