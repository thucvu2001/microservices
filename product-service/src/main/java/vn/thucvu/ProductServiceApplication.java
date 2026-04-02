package vn.thucvu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
@Slf4j
public class ProductServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Default Timezone: {}", TimeZone.getDefault().getID());
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
