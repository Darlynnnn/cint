package org.example.cint_producer.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.awaitility.Awaitility;
import org.example.cint_common.message.MessagePayload;
import org.example.cint_producer.kafka.config.KafkaTestContainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Import(KafkaTestContainersConfiguration.class)
class DataProducerIntegrationTest {

    @Autowired
    private KafkaConsumer<String, String> testConsumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        testConsumer.close();
    }

    @Test
    void scheduledProducer_publishesAValidMessagePayload_toTheRealTopic() throws Exception {
        AtomicReference<ConsumerRecord<String, String>> received = new AtomicReference<>();

        // the real @Scheduled DataProducer runs on its own fixedRate; just wait for a real tick
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records = testConsumer.poll(Duration.ofMillis(200));
                    assertThat(records.count()).isGreaterThan(0);
                    received.set(records.iterator().next());
                });

        MessagePayload payload = objectMapper.readValue(received.get().value(), MessagePayload.class);

        assertThat(payload.getRequestId()).isNotBlank();
        assertThat(UUID.fromString(payload.getRequestId())).isNotNull();
        assertThat(payload.getCreated()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(payload.getData()).isNotNull();
        assertThat(payload.getData()).isNotEqualByComparingTo(BigDecimal.ZERO);
    }
}
