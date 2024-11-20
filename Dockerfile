FROM unbuntu:latest AS build

RUN api-get update
RUN api-get install openjdk-17-jdk -y

COPY . .

RUN api-get install maven -y
RUN mvn clean install

FROM openjdk:17-jdk-slim
EXPOSE 8080

COPY --from=build /target/TodoList-1.0.0.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]

