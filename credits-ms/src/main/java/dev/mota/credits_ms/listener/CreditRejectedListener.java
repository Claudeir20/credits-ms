package dev.mota.credits_ms.listener;


import dev.mota.credits_ms.config.RabbitMQConfig;
import dev.mota.credits_ms.event.consumed.CreditRejectedEvent;
import dev.mota.credits_ms.services.CreditRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CreditRejectedListener {

    private final CreditRequestService requestService;

    @RabbitListener(queues = RabbitMQConfig.SCORE_CREDIT_REJECTED_QUEUE)
    public void handle (CreditRejectedEvent rejectEvent){
        requestService.rejectFromCreditRejectedEvent(rejectEvent);
    }
}
