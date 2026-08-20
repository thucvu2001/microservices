```text
888                                888                      888      
888                                888                      888      
888                                888                      888      
888      .d88b.   .d88b.  .d8888b  888888  8888b.  .d8888b  88888b.  
888     d88""88b d88P"88b 88K      888        "88b 88K      888 "88b 
888     888  888 888  888 "Y8888b. 888    .d888888 "Y8888b. 888  888 
888     Y88..88P Y88b 888      X88 Y88b.  888  888      X88 888  888 
88888888 "Y88P"   "Y88888  88888P'  "Y888 "Y888888  88888P' 888  888 
                      888                                            
                 Y8b d88P                                            
                  "Y88P"   
```
-- 

### 1. Install Logstash on MacOS
- [Download Kibana](https://www.elastic.co/downloads/logstash)


```bash
$ wget https://artifacts.elastic.co/downloads/logstash/logstash-8.11.4-darwin-x86_64.tar.gz
```

- Unzip file `logstash-8.11.4-darwin-x86_64.tar.gz`
```bash
  $ tar -xzf logstash-8.11.4-darwin-x86_64.tar.gz
```



- Config file `logstash.conf` for spring boot application
  ```
  # Sample Logstash configuration for creating a simple
  # Beats -> Logstash -> Elasticsearch pipeline.

  input {
    beats {
      port => 5044
    }
    file {
      path =>"/Users/tayluong/Workspace/codeBase/authentication/auth-service.log"
      start_position => "beginning"
    }
  }

  output {
    stdout {
      codec => rubydebug
    }
    elasticsearch {
      hosts => ["https://localhost:9200"]
      ssl_certificate_verification => false
      #ssl => true
      index => "elkdemoindex"
      user => "elastic"
      password => "=fTLyBrom8RQtGiDOVKS"
    }
  }
  ```

  - Config `logback.xml` at folder `resources` in spring-boot application

  ```
  <?xml version="1.0" encoding="UTF-8"?>
  <configuration>
      <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
          <encoder>
              <pattern>%d [%thread] %-5level %-50logger{40} - %msg%n</pattern>
          </encoder>
      </appender>

      <appender name="RollingFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <file>auth-service.log</file>
          <encoder>
              <pattern>%d [%thread] %-5level %-50logger{40} - %msg%n</pattern>
          </encoder>

          <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
              <fileNamePattern>auth-service-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
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

- Run logstash
```bash
  $ cd logstash-8.11.4
  $ bin/logstash
```

