package dev.mota.credits_ms.listener;

import dev.mota.credits_ms.event.consumed.CreditApprovedEvent;
import dev.mota.credits_ms.config.RabbitMQConfig;
import dev.mota.credits_ms.services.CreditRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditApprovedListener {

    private final CreditRequestService creditRequestService;

    @RabbitListener(queues = RabbitMQConfig.SCORE_CREDIT_APPROVED_QUEUE)
    public void handle(CreditApprovedEvent event) {
        creditRequestService.approveFromCreditApprovedEvent(event);
    }
}
