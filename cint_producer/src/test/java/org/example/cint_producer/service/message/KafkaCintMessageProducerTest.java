package org.example.cint_producer.service.message;

import org.example.cint_common.message.MessagePayload;
import org.example.cint_producer.service.message.impl.KafkaCintMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaCintMessageProducerTest {

    @Test
    @SuppressWarnings("unchecked")
    void sendData_publishesPayload_toConfiguredTopic() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        CintMessageProducer kafkaCintMessageProducer = new KafkaCintMessageProducer("sensor-data", kafkaTemplate);
        MessagePayload payload = new MessagePayload("req-1", LocalDateTime.now(), BigDecimal.TEN);

        kafkaCintMessageProducer.sendData(payload);

        verify(kafkaTemplate).send("sensor-data", payload);
    }
}
