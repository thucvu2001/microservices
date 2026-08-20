```text
  _____ _     _  __          __  __ _                                    _               
 | ____| |   | |/ /         |  \/  (_) ___ _ __ ___  ___  ___ _ ____   _(_) ___ ___  ___ 
 |  _| | |   | ' /   _____  | |\/| | |/ __| '__/ _ \/ __|/ _ \ '__\ \ / / |/ __/ _ \/ __|
 | |___| |___| . \  |_____| | |  | | | (__| | | (_) \__ \  __/ |   \ V /| | (_|  __/\__ \
 |_____|_____|_|\_\         |_|  |_|_|\___|_|  \___/|___/\___|_|    \_/ |_|\___\___||___/
```

### 1. Application
- Add dependency to `pom.xml`
```xml
<!-- Logging -->
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>6.6</version>
</dependency>
```

- Create file `logback-spring.xml` at folder `resources`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <include resource="org/springframework/boot/logging/logback/base.xml"/>
  <appender name="logstash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <!-- <destination>localhost:5600</destination>--> <!--local env-->
    <destination>${LOGSTASH_HOST:-localhost:5600}</destination> <!--docker env-->
    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
      <providers>
        <mdc/>
        <context/>
        <logLevel/>
        <loggerName/>
        <pattern>
          <pattern>
            {
            "appName": "api-gateway"
            }
          </pattern>
        </pattern>
        <threadName/>
        <message/>
        <logstashMarkers/>
        <stackTrace/>
      </providers>
    </encoder>
  </appender>
  <root level="info">
    <appender-ref ref="logstash"/>
  </root>
</configuration>
```


- Create file `logback-spring.xml` using multiple profiles
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Console Appender (Used for Dev & Staging) -->
    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- File Appender (Used for Staging & Prod) -->
    <appender name="File" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- ELK (Logstash) Appender -->
    <appender name="ELK" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5600</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
    </appender>

    <!-- Profile-based Logging Configuration -->
    <springProfile name="dev">
        <root level="DEBUG">
            <appender-ref ref="Console" />
        </root>
    </springProfile>

    <springProfile name="staging">
        <root level="INFO">
            <appender-ref ref="Console" />
            <appender-ref ref="File" />
        </root>
    </springProfile>

    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="File" />
            <appender-ref ref="ELK" />
        </root>
    </springProfile>

</configuration>
```

### 2. Set up ELK

-  Create file `logstash.conf` before run ELK

```logstash
input {
  tcp {
    # Socket port
    port => 5600
    codec => json
  }
}

output {
  elasticsearch {
    hosts => ["${XPACK_MONITORING_ELASTICSEARCH_HOSTS}"]
    index => "elk-index-%{appName}"
  }
}
```

- Create file `docker-compose.yml`

```yaml
version: '3.9'

services:

  api-gateway:
    container_name: api-gateway
    build:
      context: ./
      dockerfile: Dockerfile
    environment:
      - LOGSTASH_HOST=logstash:5600
    ports:
      - '4953:4953'
    depends_on:
      logstash:
        condition: service_started
    networks:
      - default
  
  elastic-search:
    image: elasticsearch:7.14.1
    container_name: elasticsearch
    restart: always
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
    networks:
      - default
  
  kibana:
    image: kibana:7.14.1
    container_name: kibana
    restart: always
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elastic-search:9200
    networks:
      - default
  
  logstash:
    image: logstash:7.14.1
    container_name: logstash
    restart: always
    ports:
      - "5600:5600" # Socket port
      - "5044:5044"
    #      - "9600:9600"
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    environment:
      - XPACK_MONITORING_ELASTICSEARCH_HOSTS=http://elastic-search:9200
      - XPACK_MONITORING_ENABLED=true
    networks:
      - default

networks:
  default:
    name: micro-kube-network
```

### 3. Check & Visualize Log

- Check [Elasticsearch](http://localhost:9200/_cat/indices)
```text
green  open .geoip_databases                WgNidAGeT1-_PGJIT7ieWg 1 0 42   0 39.3mb 39.3mb
green  open .kibana_task_manager_7.14.1_001 bb_UHjklTQqf9TG8Z9Li3Q 1 0 14 324  104kb  104kb
green  open .apm-custom-link                k57CHTTlQY-b5t8NxsE6pw 1 0  0   0   208b   208b
yellow open elk-index-api-gateway           vYWglzkwSsSAvfIuSX43sw 1 1 11   0   39kb   39kb --> It's here
green  open .apm-agent-configuration        RgQJERfdQhmeGymPRc1Baw 1 0  0   0   208b   208b
green  open .kibana_7.14.1_001              2N-aKyguTb-omULBTFL2yQ 1 0 12   0  2.1mb  2.1mb
green  open .kibana-event-log-7.14.1-000001 fEexxfBdSN-EHOHGhjkV2A 1 0  1   0  5.5kb  5.5kb
```

- Create [index pattern](http://localhost:5601/app/management/kibana/indexPatterns)

  ![create-index-pattern.png](images/create-index-pattern.png)

  ![configuration-setting.png](images/configuration-setting.png)

  ![view-result.png](images/view-result.png)



- View Log
  
  ![discovery.png](images/discovery.png)

  ![log.png](images/log.png)
