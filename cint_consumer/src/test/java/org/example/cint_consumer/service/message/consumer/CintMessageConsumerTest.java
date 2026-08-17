package org.example.cint_consumer.service.message.consumer;

import org.example.cint_common.message.MessagePayload;
import org.example.cint_consumer.service.message.procesor.MessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CintMessageConsumerTest {

    private MessageProcessor messageProcessor;
    private CintMessageConsumer cintMessageConsumer;

    @BeforeEach
    void setUp() {
        messageProcessor = mock(MessageProcessor.class);
        cintMessageConsumer = new CintMessageConsumer(messageProcessor);
    }

    @Test
    void processMessage_delegatesReceivedPayloadToMessageProcessor() {
        MessagePayload payload = new MessagePayload("req-1", LocalDateTime.now(), BigDecimal.valueOf(42.5));

        cintMessageConsumer.processMessage(payload);

        verify(messageProcessor).processMessage(payload);
    }

    @Test
    void processMessage_callsMessageProcessorExactlyOncePerMessage() {
        MessagePayload payload = new MessagePayload("req-2", LocalDateTime.now(), BigDecimal.ONE);

        cintMessageConsumer.processMessage(payload);

        verify(messageProcessor, times(1)).processMessage(payload);
        verifyNoMoreInteractions(messageProcessor);
    }
}
