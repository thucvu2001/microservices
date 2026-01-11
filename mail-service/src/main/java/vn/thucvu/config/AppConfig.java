package vn.thucvu.config;

import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private final String API_KEY = "";

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(API_KEY);
    }
}
