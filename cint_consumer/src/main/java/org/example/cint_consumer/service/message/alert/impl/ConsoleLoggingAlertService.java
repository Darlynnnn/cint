package org.example.cint_consumer.service.message.alert.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.cint_consumer.service.message.alert.AlertService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsoleLoggingAlertService implements AlertService {

    @Override
    public void alert(String message) {
        log.info(message);
    }
}
