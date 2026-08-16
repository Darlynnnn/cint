package org.example.cint_producer.service.producer;

import org.example.cint_common.message.MessagePayload;
import org.example.cint_producer.service.generator.impl.AnomalyAwareGaussNumberGenerator;
import org.example.cint_producer.service.message.CintMessageProducer;
import org.example.cint_producer.service.message.impl.KafkaCintMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataProducerTest {

    private AnomalyAwareGaussNumberGenerator generator;
    private CintMessageProducer kafkaCintMessageProducer;
    private DataProducer dataProducer;

    @BeforeEach
    void setUp() {
        generator = mock(AnomalyAwareGaussNumberGenerator.class);
        kafkaCintMessageProducer = mock(KafkaCintMessageProducer.class);
        dataProducer = new DataProducer(generator, kafkaCintMessageProducer);
    }

    @Test
    void producerData_sendsGeneratedValue_wrappedInMessagePayload() {
        when(generator.generateData()).thenReturn(BigDecimal.valueOf(42.5));

        dataProducer.producerData();

        ArgumentCaptor<MessagePayload> captor = ArgumentCaptor.forClass(MessagePayload.class);
        verify(kafkaCintMessageProducer).sendData(captor.capture());

        assertThat(captor.getValue().getData()).isEqualByComparingTo(BigDecimal.valueOf(42.5));
    }

    @Test
    void producerData_assignsAUniqueValidUuid_asRequestId_forEachMessage() {
        when(generator.generateData()).thenReturn(BigDecimal.ONE);
        ArgumentCaptor<MessagePayload> captor = ArgumentCaptor.forClass(MessagePayload.class);

        dataProducer.producerData();
        dataProducer.producerData();

        verify(kafkaCintMessageProducer, times(2)).sendData(captor.capture());
        List<MessagePayload> payloads = captor.getAllValues();

        assertThat(payloads.get(0).getRequestId()).isNotEqualTo(payloads.get(1).getRequestId());
        assertThat(UUID.fromString(payloads.get(0).getRequestId())).isNotNull();
        assertThat(UUID.fromString(payloads.get(1).getRequestId())).isNotNull();
    }

    @Test
    void producerData_setsCreatedTimestamp_closeToNow() {
        when(generator.generateData()).thenReturn(BigDecimal.ONE);
        ArgumentCaptor<MessagePayload> captor = ArgumentCaptor.forClass(MessagePayload.class);

        dataProducer.producerData();

        verify(kafkaCintMessageProducer).sendData(captor.capture());
        assertThat(captor.getValue().getCreated()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
    }
}
