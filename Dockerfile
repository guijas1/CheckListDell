# ================
# 1) BUILD (Maven + Java 21)
# ================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copia pom e baixa dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia código
COPY src ./src

# Build final
RUN mvn package -DskipTests


# ================
# 2) RUNTIME (Java 21)
# ================
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

COPY keystore.p12 /app/keystore.p12

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]