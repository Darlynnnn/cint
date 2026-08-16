package org.example.cint_producer.service.message;

import org.example.cint_common.message.MessagePayload;

public interface CintMessageProducer {
    void sendData(MessagePayload payload);
}
