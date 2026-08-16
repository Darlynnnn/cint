package org.example.cint_producer.service.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cint_common.message.MessagePayload;
import org.example.cint_producer.service.generator.DataGenerator;
import org.example.cint_producer.service.message.CintMessageProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataProducer {

    private final DataGenerator<BigDecimal> gaussDataGenerator;
    private final CintMessageProducer kafkaCintMessageProducer;

    @Scheduled(fixedRateString = "${cint.producer.fixedRate:5000}")
    public void producerData() {
        BigDecimal data = gaussDataGenerator.generateData();
        MessagePayload messagePayload = createMessagePayload(data);
        kafkaCintMessageProducer.sendData(messagePayload);
    }

    private MessagePayload createMessagePayload(BigDecimal data) {
        String requestId = UUID.randomUUID().toString();
        LocalDateTime localDateTime = LocalDateTime.now();
        return new MessagePayload(requestId, localDateTime, data);
    }
}
