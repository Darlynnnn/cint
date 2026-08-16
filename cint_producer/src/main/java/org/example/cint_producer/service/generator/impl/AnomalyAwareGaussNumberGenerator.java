package org.example.cint_producer.service.generator.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cint_producer.service.generator.AnomalyRandomizer;
import org.example.cint_producer.service.generator.DataGenerator;
import org.example.cint_producer.service.generator.GaussProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyAwareGaussNumberGenerator implements DataGenerator<BigDecimal> {

    private final Random random;
    private final GaussProperties gaussProperties;
    private final AnomalyRandomizer anomalyRandomizer;

    @Override
    public BigDecimal generateData() {
        log.info("Generating data");
        double value = calculateGaussianValue();
        return BigDecimal.valueOf(value);
    }

    private double calculateGaussianValue() {
        if (anomalyRandomizer.isAnomaly()) {
            return generateAnomaly();
        }
        return generateGaussValue();
    }


    private double generateAnomaly() {
        double mean = gaussProperties.getMean();
        double stdDev = gaussProperties.getStdDev();
        double anomalyMean = gaussProperties.getAnomalyMean();
        return mean + anomalyMean + stdDev * random.nextGaussian();
    }

    private double generateGaussValue() {
        double mean = gaussProperties.getMean();
        double stdDev = gaussProperties.getStdDev();
        return mean + stdDev * random.nextGaussian();
    }
}
