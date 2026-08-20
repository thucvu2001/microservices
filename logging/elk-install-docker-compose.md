```text
8888888888 888      888    d8P                   8888888b.                    888
888        888      888   d8P                    888  "Y88b                   888
888        888      888  d8P                     888    888                   888
8888888    888      888d88K                      888    888  .d88b.   .d8888b 888  888  .d88b.  888d888
888        888      8888888b                     888    888 d88""88b d88P"    888 .88P d8P  Y8b 888P"
888        888      888  Y88b        888888      888    888 888  888 888      888888K  88888888 888
888        888      888   Y88b                   888  .d88P Y88..88P Y88b.    888 "88b Y8b.     888
8888888888 88888888 888    Y88b                  8888888P"   "Y88P"   "Y8888P 888  888  "Y8888  888
```

- Create file `docker-compose.yml`

```
version: '3.9'

services:

  api-gateway:
    container_name: api-gateway
    build:
      context: ./
      dockerfile: Dockerfile
    ports:
      - '8081:8081'
    volumes: # mount to folder /tmp and create new directory /workspace/logs in container.
      - /tmp:/workspace/logs
  elastic-search:
    image: elasticsearch:7.14.1
    container_name: elasticsearch
    restart: always
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
  kibana:
    image: kibana:7.14.1
    container_name: kibana
    restart: always
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elastic-search:9200
  logstash:
    image: logstash:7.14.1
    container_name: logstash
    restart: always
    ports:
      - "5044:5044"
    volumes: # mount to folder /tmp and create new directory /workspace/logs in container.
      - /tmp:/workspace/logs
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    environment:
      - LOG_PATH=/workspace/logs
      - XPACK_MONITORING_ELASTICSEARCH_HOSTS=http://elastic-search:9200
      - XPACK_MONITORING_ENABLED=true

networks:
  default:
    name: elk-network
```

- Create file `logstash.conf`

  ```text
  # Sample Logstash configuration for creating a simple
  # Beats -> Logstash -> Elasticsearch pipeline.
  
  input {
    beats {
      port => 5044
    }
    file {
      path =>"${LOG_PATH}/api-gateway.log"
      start_position => "beginning"
    }
  }
  
  output {
    stdout {
      codec => rubydebug
    }
    elasticsearch {
      hosts => ["${XPACK_MONITORING_ELASTICSEARCH_HOSTS}"]
      index => "elkdemoindex"
    }
  }
  ```

- Create file `logback.xml` at folder `resources` in spring-boot application

  ```
  <?xml version="1.0" encoding="UTF-8"?>
  <configuration>
      <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
          <encoder>
              <pattern>%d [%thread] %-5level %-50logger{40} - %msg%n</pattern>
          </encoder>
      </appender>

      <appender name="RollingFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <file>api-gateway.log</file>
          <encoder>
              <pattern>%d [%thread] %-5level %-50logger{40} - %msg%n</pattern>
          </encoder>

          <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
              <fileNamePattern>api-gateway-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
              <maxFileSize>1MB</maxFileSize>
              <maxHistory>30</maxHistory>
              <totalSizeCap>10MB</totalSizeCap>
              <cleanHistoryOnStart>true</cleanHistoryOnStart>
          </rollingPolicy>
      </appender>

      <root level="INFO">
          <appender-ref ref="Console" />
          <appender-ref ref="RollingFile" />
      </root>
  </configuration>
  ```

