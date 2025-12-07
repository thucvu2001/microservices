package vn.thucvu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("Default Timezone: " + TimeZone.getDefault().getID());
        SpringApplication.run(AccountServiceApplication.class, args);
    }

}
