# -------- Stage 1: build --------
FROM maven:3.9.7-eclipse-temurin-21 AS build
WORKDIR /app

# Copia dados de build do Maven
COPY pom.xml .
COPY src ./src

# Compila projeto sem rodar testes
RUN mvn -B package -DskipTests

# -------- Stage 2: runtime --------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o artefato gerado pelo estágio 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Ativa perfil "docker" se você quiser propriedades específicas
# Pode ser removido se não usar perfis
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java","-jar","/app/app.jar"] 