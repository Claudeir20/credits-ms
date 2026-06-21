package dev.mota.credits_ms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;@Configuration
public class RabbitMQConfig {

    public static final String CREDIT_EXCHANGE = "credit.exchange";
    public static final String CREDIT_APPROVED_EXCHANGE = "credit.aproved.exchange";
    public static final String CREDIT_REJECTED_EXCHANGE = "credit.reject.exchange";


    public static final String CREDIT_REQUESTED_QUEUE = "credit.requested.queue";
    public static final String CREDIT_APPROVED_QUEUE = "credit.approved.queue";
    public static final String CREDIT_REJECTED_QUEUE = "credit.rejected.queue";
    public static final String SCORE_CREDIT_APPROVED_QUEUE = "score.credit.approved.queue";
    public static final String SCORE_CREDIT_REJECTED_QUEUE = "score.credit.rejected.queue";


    public static final String CREDIT_REQUESTED_ROUTING_KEY = "credit.requested";
    public static final String CREDIT_APPROVED_ROUTING_KEY = "credit.approved";
    public static final String CREDIT_REJECTED_ROUTING_KEY = "credit.rejected";


    @Bean
    public TopicExchange creditExchange(){
        return new TopicExchange(CREDIT_EXCHANGE);
    }

    @Bean
    public TopicExchange creditApprovedExchange(){
        return new TopicExchange(CREDIT_APPROVED_EXCHANGE);
    }

    @Bean
    public TopicExchange creditRejectedExchange(){
        return new TopicExchange(CREDIT_REJECTED_EXCHANGE);
    }


    @Bean
    public Queue creditRequestedQueue(){
        return QueueBuilder.durable(CREDIT_REQUESTED_QUEUE).build();
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
        return QueueBuilder.durable(SCORE_CREDIT_APPROVED_QUEUE).build();
    }

    @Bean
    public Queue scoreCreditRejectedQueue() {
        return QueueBuilder.durable(SCORE_CREDIT_REJECTED_QUEUE).build();
    }




    @Bean
    public Binding creditRequestedBing(
            Queue creditRequestedQueue,
            TopicExchange creditExchange
    ){
        return BindingBuilder
                .bind(creditRequestedQueue)
                .to(creditExchange)
                .with(CREDIT_REQUESTED_ROUTING_KEY);

    }

    @Bean
    public Binding creditApprovedBing(
            Queue creditApprovedQueue,
            TopicExchange creditExchange
    ){
        return BindingBuilder
                .bind(creditApprovedQueue)
                .to(creditExchange)
                .with(CREDIT_APPROVED_ROUTING_KEY);

    }

    @Bean
    public Binding creditRejectedBing(
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
