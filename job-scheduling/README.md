```text
    ____        __       __                    __      __  
   / __ )____ _/ /______/ /_                  / /___  / /_ 
  / __  / __ `/ __/ ___/ __ \   ______   __  / / __ \/ __ \
 / /_/ / /_/ / /_/ /__/ / / /  /_____/  / /_/ / /_/ / /_/ /
/_____/\__,_/\__/\___/_/ /_/            \____/\____/_.___/ 
```
## Prerequisite
- Cài đặt JDK 17+ nếu chưa thì [cài đặt JDK](https://tayjava.vn/cai-dat-jdk-tren-macos-window-linux-ubuntu/)
- Install Maven 3.5+ nếu chưa thì [cài đặt Maven](https://tayjava.vn/cai-dat-maven-tren-macos-window-linux-ubuntu/)
- Install IntelliJ nếu chưa thì [cài đặt IntelliJ](https://tayjava.vn/cai-dat-intellij-tren-macos-va-window/)

## Technical Stacks
- Java 17
- Spring Boot 3.2.3
- PostgresSQL
- Batch
- Job
- Maven 3.5+
- Lombok
- DevTools
- Docker, Docker compose
- ELK
- Grafana
- Prometheus

## How to build Job Scheduling
- [Cron Job là gì? Cấu Trúc Cron Job](https://tayjava.vn/cron-job-la-gi-cau-truc-cron-job)
- [Spring Batch là gì? Batch Job với Spring Boot](https://tayjava.vn/spring-batch-la-gi-batch-job-voi-spring-boot)

### 1. Using @Scheduled
Dùng để thực hiện với các mục đích đơn giản như: thăm dò định kỳ, làm mới bộ đệm hoặc các tác vụ nhẹ.

- Enable Scheduling
```java
package vn.tayjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BatchJobApplication {
  public static void main(String[] args) {
    SpringApplication.run(BatchJobApplication.class, args);
  }
}
```

- Create Task
```java
package vn.tayjava.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SchedulerTask {

    Logger logger = LoggerFactory.getLogger(SchedulerTask.class);

    @Scheduled(fixedRate = 5000)  // Runs every 5 seconds
    public void fixedRateTask() {
        logger.info("Fixed Rate Task: {}", LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 5000)  // Runs 5 seconds after the last execution finishes
    public void fixedDelayTask() {
        logger.info("Fixed Delay Task: {}", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0/1 * * * ?") // Runs every 1 minute
    public void cronTask() {
        logger.info("Cron Task: {}", LocalDateTime.now());
    }
}
```

### 2. Using Quartz Scheduler
Dùng để thực hiện với các mục đích phức tạp hơn như: lập lịch phức tạp, phân tán

- Add Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

- Define a Quartz Job
```java
package vn.tayjava.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Advanced Scheduling
 */
public class QuartzJob implements Job {

    Logger log = LoggerFactory.getLogger(QuartzJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing Quartz Job: {}", LocalDateTime.now());
    }
}
```

- Quartz Config
```java
package vn.tayjava.job;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(QuartzJob.class)
                .withIdentity("quartzJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger trigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("quartzTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10) // Run every 10 seconds
                        .repeatForever())
                .build();
    }
}
```


### 3. Using Spring Batch (For Large-Scale Job Processing)
- Add dependency
```xml
<dependencies>
  <!-- Batch -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
  </dependency>
  
  <!-- H2-DATABASE -->
  <dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
  </dependency>
  
  <!-- JPA -->
  <dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
</dependencies>
```

- Config H2 database
```yaml
spring:
  batch:
    job:
      enabled: true
    jdbc:
      initialize-schema: always
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
    hikari.maximum-pool-size: 10
  h2:
    console:
      enabled: true
      path: /h2-console
```

- Create model
```java
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
```

- Create listener `JobCompletionNotificationListener`

```java
package vn.tayjava.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import vn.tayjava.batch.Customer;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final JdbcTemplate jdbcTemplate;

    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            jdbcTemplate
                    .query("SELECT id, first_name, last_name FROM customer", new DataClassRowMapper<>(Customer.class))
                    .forEach(customer -> log.info("Found <{}> in the database.", customer));
        }
    }
}
```

- Create process `CustomerItemProcessor`

```java
package vn.tayjava.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class CustomerItemProcessor implements ItemProcessor<vn.tayjava.batch.Customer, vn.tayjava.batch.Customer> {

    private static final Logger log = LoggerFactory.getLogger(CustomerItemProcessor.class);

    @Override
    public vn.tayjava.batch.Customer process(final vn.tayjava.batch.Customer customer) {
        final String firstName = customer.getFirstName().toUpperCase();
        final String lastName = customer.getFirstName().toUpperCase();

        final Customer transformedCustomer = new Customer(firstName, lastName);

        log.info("Converting ({}) into ({})", customer, transformedCustomer);

        return transformedCustomer;
    }

}
```

- Config batch `BatchConfiguration`
```java
package vn.tayjava.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    @Bean
    public FlatFileItemReader<Customer> reader() {
        log.info("reader Customer data...");
        ClassPathResource resource = new ClassPathResource("sample-data.csv");
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .resource(resource)
                .delimited()
                .names("firstName", "lastName")
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public CustomerItemProcessor processor() {
        return new CustomerItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> writer(DataSource dataSource) {
        log.info("writer Customer data...");
        return new JdbcBatchItemWriterBuilder<Customer>()
                .sql("INSERT INTO customer (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1, JobCompletionNotificationListener listener) {
        log.info("importUserJob Customer data...");
        return new JobBuilder("importUserJob", jobRepository)
                .listener(listener)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                      FlatFileItemReader<Customer> reader, CustomerItemProcessor processor, JdbcBatchItemWriter<Customer> writer) {
        log.info("step1 Customer data...");
        return new StepBuilder("step1", jobRepository)
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        log.info("transactionManager Customer data...");
        return new DataSourceTransactionManager(dataSource);
    }
}
```

## Build application
```bash
mvn clean package -P dev|test|uat|prod
```

## Run application
- Maven statement
```bash
cd job-scheduling
./mvnw spring-boot:run
```
- Jar statement
```bash
cd account
java -jar target/job-scheduling.jar
```

- Docker
```bash
docker build -t job-scheduling ./account
docker run -d job-scheduling:latest job-scheduling
```

## Package application
```bash
docker build -t job-scheduling ./job-scheduling
```

## Health check
```bash
curl --location 'http://localhost:8089/actuator/health'
```
