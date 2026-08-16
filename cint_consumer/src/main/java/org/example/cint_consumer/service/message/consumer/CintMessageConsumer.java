package org.example.cint_consumer.service.message.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cint_common.message.MessagePayload;
import org.example.cint_consumer.service.message.procesor.MessageProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CintMessageConsumer {

    private final MessageProcessor messageProcessor;

    @KafkaListener(topics = "${cint.consumer.topic}")
    public void processMessage(MessagePayload message) {
        log.info("Received message: {}", message);
        messageProcessor.processMessage(message);
    }
}
