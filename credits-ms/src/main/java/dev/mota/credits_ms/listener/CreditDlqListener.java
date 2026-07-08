package dev.mota.credits_ms.listener;

import dev.mota.credits_ms.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditDlqListener {

    @RabbitListener(queues = RabbitMQConfig.CREDIT_DLQ)
    public void handleDeadLetter(String payload) {
        log.error("Message sent to DLQ: {}", payload);
    }
}