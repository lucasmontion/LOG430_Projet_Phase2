# Étape 1 : compilation (image avec Maven + JDK)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# D'abord les POM seuls : Docker met en cache le téléchargement des dépendances
# tant que les POM ne changent pas (les builds suivants sont beaucoup plus rapides)
COPY pom.xml .
COPY commande-service/pom.xml commande-service/
RUN mvn -q -f commande-service/pom.xml dependency:go-offline

# Ensuite le code, puis le jar
COPY commande-service/src commande-service/src
RUN mvn -q -f commande-service/pom.xml package -DskipTests

# Étape 2 : exécution (image légère, JRE seulement, sans Maven ni code source)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/commande-service/target/commande-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]