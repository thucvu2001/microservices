```text
    ___         __  __               _             __  _           
   /   | __  __/ /_/ /_  ____  _____(_)___  ____ _/ /_(_)___  ____ 
  / /| |/ / / / __/ __ \/ __ \/ ___/ /_  / / __ `/ __/ / __ \/ __ \
 / ___ / /_/ / /_/ / / / /_/ / /  / / / /_/ /_/ / /_/ / /_/ / / / /
/_/  |_\__,_/\__/_/ /_/\____/_/  /_/ /___/\__,_/\__/_/\____/_/ /_/ 
```

## Prerequisite
- Cài đặt JDK 17+ nếu chưa thì [cài đặt JDK](https://tayjava.vn/cai-dat-jdk-tren-macos-window-linux-ubuntu/)
- Install Maven 3.5+ nếu chưa thì [cài đặt Maven](https://tayjava.vn/cai-dat-maven-tren-macos-window-linux-ubuntu/)
- Install IntelliJ nếu chưa thì [cài đặt IntelliJ](https://tayjava.vn/cai-dat-intellij-tren-macos-va-window/)

## Technical Stacks
- Java 17
- Spring Boot 3.2.3
- PostgresSQL
- JPA
- Maven 3.5+
- Lombok
- DevTools
- Docker, Docker compose
- ELK
- Grafana
- Prometheus

## Build application
```bash
mvn clean package -P dev|test|uat|prod
```

## Run application
- Maven statement
```bash
./mvnw spring-boot:run
```

- Jar statement
```bash
java -jar target/authorization-service.jar
```

- Docker
```bash
docker build -t authorization-service .
docker run -d authorization-service:latest authorization-service
```

## Package application
```bash
docker build -t authorization-service .
```

## Health check
```bash
curl --location 'http://localhost:80890/actuator/health'
```
