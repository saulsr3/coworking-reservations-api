# ---- Etapa 1: Build ----
# Usamos una imagen con Maven + JDK 17 SOLO para compilar; esta imagen es
# pesada (~500MB+) y no queremos que forme parte de la imagen final.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos solo el pom.xml primero y descargamos dependencias ANTES de copiar
# el código fuente. Esto aprovecha el cache de capas de Docker: si solo
# cambia el código (no las dependencias), Docker reutiliza esta capa pesada
# de descarga en vez de repetirla en cada build.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Etapa 2: Runtime ----
# Imagen final mucho más liviana: solo el JRE (no el JDK completo, no
# Maven), suficiente para EJECUTAR la app, no para compilarla.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiamos únicamente el .jar ya compilado desde la etapa anterior — el
# código fuente y Maven se quedan atrás, no viajan a la imagen final.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]