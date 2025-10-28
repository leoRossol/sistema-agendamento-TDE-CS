# Dockerfile para o microserviço scheduler-svc (US-04)
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copiar arquivos do Maven Wrapper
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copiar pom.xml e baixar dependências
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src src

# Compilar e fazer build
RUN ./mvnw clean package -DskipTests

# Copiar JAR gerado
RUN cp target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

