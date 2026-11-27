# ==============================================================================
# ETAPA 1: BUILD (Compilación con Eclipse Temurin JDK 21)
# ==============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copiar wrapper y configuración de dependencias primero para cachear capas
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Asegurar terminaciones de línea LF y permisos de ejecución en mvnw
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Copiar el código fuente completo
COPY src ./src

# Compilar empaquetando el artefacto target/app.jar omitiendo tests
RUN ./mvnw clean package -DskipTests

# ==============================================================================
# ETAPA 2: RUNTIME (Ejecución ligera con Eclipse Temurin JRE 21)
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Crear usuario y grupo de sistema sin privilegios de root por seguridad
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copiar el JAR generado en la etapa de compilación
COPY --from=builder /app/target/app.jar app.jar

# Documentar el puerto expuesto
EXPOSE 8080

# Opciones de JVM optimizadas para contenedores (control de memoria en Render)
ENV PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Ejecutar escuchando en el puerto dinámico asignado por Render (${PORT:-8080})
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]
