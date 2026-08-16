package org.example.cint_consumer.service.message.procesor.impl;

import org.example.cint_common.message.MessagePayload;
import org.example.cint_consumer.service.message.alert.AlertService;
import org.example.cint_consumer.util.SlidingWindowZValueCalculator;
import org.example.cint_consumer.util.ZValueCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultMessageProcessorTest {

    private static final int THRESHOLD = 3;

    private ZValueCalculator slidingWindowZValueCalculator;
    private AlertService alertService;
    private DefaultMessageProcessor processor;

    @BeforeEach
    void setUp() {
        slidingWindowZValueCalculator = mock(SlidingWindowZValueCalculator.class);
        alertService = mock(AlertService.class);
        processor = new DefaultMessageProcessor(slidingWindowZValueCalculator, alertService, THRESHOLD);
    }

    @Test
    void processMessage_passesPayloadDataToZValueCalculator() {
        MessagePayload payload = new MessagePayload("req-1", LocalDateTime.now(), BigDecimal.valueOf(42));
        when(slidingWindowZValueCalculator.calculateZScore(any())).thenReturn(BigDecimal.ZERO);

        processor.processMessage(payload);

        verify(slidingWindowZValueCalculator).calculateZScore(BigDecimal.valueOf(42));
    }

    @Test
    void processMessage_doesNotThrow_regardlessOfZScoreMagnitude() {
        MessagePayload payload = new MessagePayload("req-2", LocalDateTime.now(), BigDecimal.valueOf(999));
        when(slidingWindowZValueCalculator.calculateZScore(any())).thenReturn(BigDecimal.valueOf(10));

        assertThatCode(() -> processor.processMessage(payload)).doesNotThrowAnyException();
    }

    @Test
    void withinThreshold_sendsNormalAlert_notAnomalyAlert() {
        MessagePayload payload = new MessagePayload("req-3", LocalDateTime.now(), BigDecimal.valueOf(50));
        when(slidingWindowZValueCalculator.calculateZScore(any())).thenReturn(BigDecimal.valueOf(2.9));

        processor.processMessage(payload);

        verify(alertService).alert(argThat(message ->
                message.contains("Status: OK") && !message.contains("ANOMALY")));
    }

    @Test
    void exceedsThreshold_sendsAnomalyAlert() {
        MessagePayload payload = new MessagePayload("req-4", LocalDateTime.now(), BigDecimal.valueOf(5000));
        when(slidingWindowZValueCalculator.calculateZScore(any())).thenReturn(BigDecimal.valueOf(3.1));

        processor.processMessage(payload);

        verify(alertService).alert(argThat(message ->
                message.contains("ANOMALY DETECTED!") && message.contains("ALERT: Significant deviation detected.")));
    }

    @Test
    void exactlyAtThreshold_isNotAnAnomaly() {
        MessagePayload payload = new MessagePayload("req-5", LocalDateTime.now(), BigDecimal.valueOf(50));
        when(slidingWindowZValueCalculator.calculateZScore(any())).thenReturn(BigDecimal.valueOf(THRESHOLD));

        processor.processMessage(payload);

        verify(alertService).alert(argThat(message -> !message.contains("ANOMALY")));
    }

    @Test
    void alertServiceIsCalledExactlyOncePerMessage() {
        MessagePayload payload = new MessagePayload("req-7", LocalDateTime.now(), BigDecimal.valueOf(1));
        when(slidingWindowZValueCalculator.calculateZScore(any())).thenReturn(BigDecimal.ZERO);

        processor.processMessage(payload);

        verify(alertService, times(1)).alert(any());
    }
}
