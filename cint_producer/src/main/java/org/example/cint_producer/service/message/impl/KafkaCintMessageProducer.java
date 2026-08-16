package org.example.cint_producer.service.message.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cint_common.message.MessagePayload;
import org.example.cint_producer.service.message.CintMessageProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
@Slf4j
public class KafkaCintMessageProducer implements CintMessageProducer {

    private final String topic;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendData(MessagePayload payload) {
        log.info("Sending data to topic: {}", topic);
        CompletableFuture<SendResult<String, Object>> futureResponse = kafkaTemplate.send(topic, payload);
        futureResponse.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send data {} to topic: {}", payload, topic);
            } else {
                log.info("Data sent to the message queue, partition: {}, offset: {}",
                        result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}
