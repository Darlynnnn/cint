package org.example.cint_consumer.service.message.procesor;

import org.example.cint_common.message.MessagePayload;

public interface MessageProcessor {
    void processMessage(MessagePayload messagePayload);
}
