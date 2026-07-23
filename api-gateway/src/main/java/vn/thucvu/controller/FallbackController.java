package vn.thucvu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackAuth() {
        return ResponseEntity.status(503).body("Authentication service unavailable");
    }

    @RequestMapping(value = "/account", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackAccount() {
        return ResponseEntity.status(503).body("Account service unavailable");
    }

    @RequestMapping(value = "/mail", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackMail() {
        return ResponseEntity.status(503).body("Mail service unavailable");
    }

    @RequestMapping(value = "/product", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackProduct() {
        return ResponseEntity.status(503).body("Product service unavailable");
    }

    @RequestMapping(value = "/order", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackOrder() {
        return ResponseEntity.status(503).body("Order service unavailable");
    }

    @RequestMapping(value = "/payment", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackPayment() {
        return ResponseEntity.status(503).body("Payment service unavailable");
    }

    @RequestMapping(value = "/inventory", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackInventory() {
        return ResponseEntity.status(503).body("Inventory service unavailable");
    }

    @RequestMapping(value = "/notification", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackNotification() {
        return ResponseEntity.status(503).body("Notification service unavailable");
    }

    @RequestMapping(value = "/author", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> fallbackAuthor() {
        return ResponseEntity.status(503).body("Authorization service unavailable");
    }
}
