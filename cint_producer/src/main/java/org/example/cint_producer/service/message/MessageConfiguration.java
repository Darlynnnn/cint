package org.example.cint_producer.service.message;

import org.example.cint_producer.service.message.impl.KafkaCintMessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class MessageConfiguration {

    @Value(("${cint.producer.topic}"))
    private String topic;

    @Bean
    public KafkaCintMessageProducer bigDecimalKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaCintMessageProducer(topic, kafkaTemplate);
    }
}
