package vn.thucvu.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.thucvu.service.EmailService;

@RestController
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Operation(summary = "Send simple mail", description = "Send email with plain text")
    @GetMapping("/send")
    public String sendEmail(@RequestParam String toEmail, @RequestParam String subject, @RequestParam String body) {
        return emailService.sendEmail(toEmail, subject, body);
    }
}