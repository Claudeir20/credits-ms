package dev.mota.credits_ms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CREDIT_EXCHANGE = "credit.exchange";


    public static final String CREDIT_APPROVED_QUEUE = "credit.approved.queue";
    public static final String CREDIT_REJECTED_QUEUE = "credit.rejected.queue";
    public static final String SCORE_CREDIT_APPROVED_QUEUE = "score.credit.approved.queue";
    public static final String SCORE_CREDIT_REJECTED_QUEUE = "score.credit.rejected.queue";


    public static final String CREDIT_REQUESTED_ROUTING_KEY = "credit.requested";
    public static final String CREDIT_APPROVED_ROUTING_KEY = "credit.approved";
    public static final String CREDIT_REJECTED_ROUTING_KEY = "credit.rejected";

    public static final String CREDIT_DLX = "credit.dlx";
    public static final String CREDIT_DLQ = "credit.dlq";
    public static final String CREDIT_DLQ_ROUTING_KEY = "credit.dlq";



    @Bean
    public TopicExchange creditExchange(){
        return new TopicExchange(CREDIT_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange(){
        return new DirectExchange(CREDIT_DLX);
    }


    @Bean
    public Queue creditDlq(){
        return QueueBuilder.durable(CREDIT_DLQ).build();
    }


    @Bean
    public Queue creditApprovedQueue(){
        return QueueBuilder.durable(CREDIT_APPROVED_QUEUE).build();
    }

    @Bean
    public Queue creditRejectedQueue(){
        return QueueBuilder.durable(CREDIT_REJECTED_QUEUE).build();
    }

    @Bean
    public Queue scoreCreditApprovedQueue() {
        return QueueBuilder.durable(SCORE_CREDIT_APPROVED_QUEUE)
                .deadLetterExchange(CREDIT_DLX)
                .deadLetterRoutingKey(CREDIT_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue scoreCreditRejectedQueue() {
        return QueueBuilder.durable(SCORE_CREDIT_REJECTED_QUEUE)
                .deadLetterExchange(CREDIT_DLX)
                .deadLetterRoutingKey(CREDIT_DLQ_ROUTING_KEY)
                .build();
    }



    @Bean
    public Binding creditDlqBinding(
            Queue creditDlq,
            DirectExchange deadLetterExchange
    ){
        return BindingBuilder
                .bind(creditDlq)
                .to(deadLetterExchange)
                .with(CREDIT_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding creditApprovedBinding(
            Queue creditApprovedQueue,
            TopicExchange creditExchange
    ){
        return BindingBuilder
                .bind(creditApprovedQueue)
                .to(creditExchange)
                .with(CREDIT_APPROVED_ROUTING_KEY);

    }

    @Bean
    public Binding creditRejectedBinding(
            Queue creditRejectedQueue,
            TopicExchange creditExchange
    ){
        return BindingBuilder
                .bind(creditRejectedQueue)
                .to(creditExchange)
                .with(CREDIT_REJECTED_ROUTING_KEY);

    }


    @Bean
    public Binding scoreCreditApprovedBinding(Queue scoreCreditApprovedQueue, TopicExchange creditExchange) {
        return BindingBuilder
                .bind(scoreCreditApprovedQueue)
                .to(creditExchange)
                .with(CREDIT_APPROVED_ROUTING_KEY);
    }

    @Bean
    public Binding scoreCreditRejectedBinding(Queue scoreCreditRejectedQueue, TopicExchange creditExchange) {
        return BindingBuilder
                .bind(scoreCreditRejectedQueue)
                .to(creditExchange)
                .with(CREDIT_REJECTED_ROUTING_KEY);
    }



    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
