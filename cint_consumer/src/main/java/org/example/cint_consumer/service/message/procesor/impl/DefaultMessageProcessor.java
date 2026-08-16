package org.example.cint_consumer.service.message.procesor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.cint_common.message.MessagePayload;
import org.example.cint_consumer.service.message.alert.AlertService;
import org.example.cint_consumer.service.message.procesor.MessageProcessor;
import org.example.cint_consumer.util.ZValueCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
public class DefaultMessageProcessor implements MessageProcessor {

    private static final String MESSAGE = "[%s] Data point: %s | Status: OK | Z-score: %s";
    private static final String ANOMALY_MESSAGE = "[%s] Data point: %s | Status: ANOMALY DETECTED! | Z-score: %s | ALERT: Significant deviation detected.";

    private final ZValueCalculator zValueCalculator;
    private final AlertService alertService;
    private final int threshold;


    public DefaultMessageProcessor(ZValueCalculator zValueCalculator, AlertService alertService, @Value("${cint.anomaly.threshold}") int threshold) {
        this.zValueCalculator = zValueCalculator;
        this.alertService = alertService;
        this.threshold = threshold;
    }

    @Override
    public void processMessage(MessagePayload messagePayload) {
        log.info("Processing message: with requestId: {}", messagePayload.getRequestId());
        BigDecimal zValue = zValueCalculator.calculateZScore(messagePayload.getData());
        BigDecimal displayData = messagePayload.getData().setScale(2, RoundingMode.HALF_UP);
        BigDecimal displayZValue = zValue.setScale(2, RoundingMode.HALF_UP);
        if (isAnomaly(zValue)) {
            alertService.alert(String.format(ANOMALY_MESSAGE, Instant.now(), displayData, displayZValue));
        } else {
            alertService.alert(String.format(MESSAGE, Instant.now(), displayData, displayZValue));
        }
        log.info("Finished processing message: with requestId: {}", messagePayload.getRequestId());
    }

    private boolean isAnomaly(BigDecimal zValue) {
        return zValue.compareTo(BigDecimal.valueOf(threshold)) > 0;
    }
}
