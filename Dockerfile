# Etap 1: budujemy obraz JDK 21 + Maven build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Kopiujemy pliki projektu
COPY . .

# Budujemy aplikację w Dockerze
RUN ./mvnw clean package -DskipTests

# Etap 2: obraz uruchomieniowy — tylko JRE (lekki)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Kopiujemy gotowego jara z etapu build
COPY --from=build /app/target/*.jar app.jar

# Eksponujemy port (Spring domyślnie na 8080)
EXPOSE 8080

# Domyślne polecenie do uruchomienia
ENTRYPOINT ["java", "-jar", "app.jar"]
