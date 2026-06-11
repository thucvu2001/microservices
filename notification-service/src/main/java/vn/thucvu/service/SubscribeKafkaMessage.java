package vn.thucvu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SUBSCRIBE-KAFKA-MESSAGE")
public class SubscribeKafkaMessage {

    private final OneSignalService oneSignalService;

    /**
     * Listen event from payment success
     */
    @KafkaListener(topics = "${spring.kafka.order-success}", groupId = "update-order-status-group")
    public void handleEventPushNotification(String message) {
        log.info("handleEventPushNotification called,); message: {}", message);

        // get device token by customerId
        String deviceToken = "1";

        oneSignalService.sendPushNotification(List.of(deviceToken), "Place order successfully");
    }
}
