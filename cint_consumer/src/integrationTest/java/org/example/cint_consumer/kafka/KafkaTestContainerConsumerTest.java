package org.example.cint_consumer.kafka;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.awaitility.Awaitility;
import org.example.cint_common.message.MessagePayload;
import org.example.cint_consumer.kafka.config.KafkaTestContainersConfiguration;
import org.example.cint_consumer.service.message.alert.impl.ConsoleLoggingAlertService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Import(KafkaTestContainersConfiguration.class)
class KafkaTestContainerConsumerTest {

    @Autowired
    private KafkaTemplate<String, MessagePayload> kafkaTemplate;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogCapture() {
        Logger logger = (Logger) LoggerFactory.getLogger(ConsoleLoggingAlertService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogCapture() {
        ((Logger) LoggerFactory.getLogger(ConsoleLoggingAlertService.class)).detachAppender(logAppender);
    }

    @Test
    void producedDataFlowsThroughRealConsumerPipeline_andAnomalyIsDetected() {
        for (int i = 0; i < 10; i++) {
            send(BigDecimal.valueOf(100 + (i % 3)));
        }

        send(BigDecimal.valueOf(100_000));

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(logAppender.list)
                        .extracting(ILoggingEvent::getFormattedMessage)
                        .anyMatch(message -> message.contains("ANOMALY DETECTED!")
                                && message.contains("ALERT: Significant deviation detected.")));
    }

    private void send(BigDecimal data) {
        MessagePayload payload = new MessagePayload(UUID.randomUUID().toString(), LocalDateTime.now(), data);
        kafkaTemplate.send("sensor-data", payload);
    }
}
