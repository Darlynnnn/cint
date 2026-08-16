package org.example.cint_common.message;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessagePayloadTest {

    @Test
    void allArgsConstructor_assignsAllFields() {
        LocalDateTime created = LocalDateTime.now();

        MessagePayload payload = new MessagePayload("req-1", created, BigDecimal.valueOf(42.5));

        assertEquals("req-1", payload.getRequestId());
        assertEquals(created, payload.getCreated());
        assertEquals(BigDecimal.valueOf(42.5), payload.getData());
    }

    @Test
    void noArgsConstructor_leavesFieldsUnset() {
        MessagePayload payload = new MessagePayload();

        assertNull(payload.getRequestId());
        assertNull(payload.getCreated());
        assertNull(payload.getData());
    }

    @Test
    void setters_updateFields() {
        MessagePayload payload = new MessagePayload();
        LocalDateTime created = LocalDateTime.now();

        payload.setRequestId("req-2");
        payload.setCreated(created);
        payload.setData(BigDecimal.ONE);

        assertEquals("req-2", payload.getRequestId());
        assertEquals(created, payload.getCreated());
        assertEquals(BigDecimal.ONE, payload.getData());
    }
}
