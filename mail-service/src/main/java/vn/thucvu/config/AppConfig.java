package vn.thucvu.config;

import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private final String API_KEY = "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";

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(API_KEY);
    }
}
