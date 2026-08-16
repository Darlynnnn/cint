package org.example.cint_producer.service.generator.impl;

import org.example.cint_producer.service.generator.GaussProperties;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class AnomalyRandomizerImplTest {

    private final Random random = new Random();
    
    @Test
    void zeroChance_neverReturnsAnomaly() {
        GaussProperties props = new GaussProperties();
        props.setAnomalyChance(0.0);
        AnomalyRandomizerImpl randomizer = new AnomalyRandomizerImpl(props,random);

        for (int i = 0; i < 1000; i++) {
            assertThat(randomizer.isAnomaly()).isFalse();
        }
    }

    @Test
    void fullChance_alwaysReturnsAnomaly() {
        GaussProperties props = new GaussProperties();
        props.setAnomalyChance(1.0);
        AnomalyRandomizerImpl randomizer = new AnomalyRandomizerImpl(props,random);

        for (int i = 0; i < 1000; i++) {
            assertThat(randomizer.isAnomaly()).isTrue();
        }
    }

    @Test
    void moderateChance_producesRoughlyTheExpectedRate() {
        GaussProperties props = new GaussProperties();
        props.setAnomalyChance(0.3);
        AnomalyRandomizerImpl randomizer = new AnomalyRandomizerImpl(props,random);

        int iterations = 10_000;
        long anomalyCount = 0;
        for (int i = 0; i < iterations; i++) {
            if (randomizer.isAnomaly()) {
                anomalyCount++;
            }
        }
        double observedRate = (double) anomalyCount / iterations;

        assertThat(observedRate).isCloseTo(0.3, offset(0.02));
    }
}
