package vn.thucvu.service;

import com.google.gson.Gson;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SendGrid sendGrid;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public String sendEmail(String toEmail, String subject, String body) {
        Email from = new Email("vuthuc3152001@gmail.com", "Thuc Vu"); // Email của bạn
        Email to = new Email(toEmail);

        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            // Kiểm tra kết quả phản hồi từ SendGrid
            if (response.getStatusCode() == 202) {
                return "Email sent successfully!";
            } else {
                return "Failed to send email: " + response.getBody();
            }

        } catch (IOException e) {
            return "Error occurred while sending email: " + e.getMessage();
        }
    }

    @KafkaListener(topics = "send-email-register-topic", groupId = "send-email-register-group") // groupId để các instance khác không dùng chung 1 message
    public void sendConfirmEmail(String message) throws IOException {
        log.info("Send email confirmation: {}", message);
        MessageDTO messageDTO = new Gson().fromJson(message, MessageDTO.class);

        Email from = new Email("vuthuc3152001@gmail.com", "Thuc Vu");
        Email to = new Email(messageDTO.getEmail());

        String subject = "Confirm email account";

        Map<String, String> dynamicTemplateData = new HashMap<>();
        dynamicTemplateData.put("name", messageDTO.getUsername());
        dynamicTemplateData.put("email", messageDTO.getEmail());
        dynamicTemplateData.put("verification_link", "http://localhost:4953/account/user/confirm-email?secretCode=" + messageDTO.getSecretCode());

        // mail server
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        dynamicTemplateData.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);
        mail.setTemplateId("d-2c7f7835f0164b4eb2f0cd756f315615");

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        if (response.getStatusCode() == 202) {
            log.info("Email send successfully!");
        } else {
            log.error("Failed to send email: {}", to);
        }

    }

    @Getter
    @Setter
    private static class MessageDTO {
        private long id;
        private String email;
        private String username;
        private String secretCode;
    }
}
