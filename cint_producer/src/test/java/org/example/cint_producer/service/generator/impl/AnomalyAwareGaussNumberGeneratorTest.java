package org.example.cint_producer.service.generator.impl;

import org.example.cint_producer.service.generator.AnomalyRandomizer;
import org.example.cint_producer.service.generator.GaussProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnomalyAwareGaussNumberGeneratorTest {

    private GaussProperties gaussProperties;
    private AnomalyRandomizer anomalyRandomizer;

    @BeforeEach
    void setUp() {
        gaussProperties = new GaussProperties();
        gaussProperties.setMean(100);
        gaussProperties.setStdDev(0); // no randomness by default; overridden per-test where needed
        gaussProperties.setAnomalyMean(200);
        gaussProperties.setAnomalyChance(0.05);

        anomalyRandomizer = mock(AnomalyRandomizer.class);
    }

    @Test
    void normalCase_returnsExactlyTheConfiguredMean_whenStdDevIsZero() {
        AnomalyAwareGaussNumberGenerator generator =
                new AnomalyAwareGaussNumberGenerator(new Random(), gaussProperties, anomalyRandomizer);
        when(anomalyRandomizer.isAnomaly()).thenReturn(false);

        BigDecimal value = generator.generateData();

        assertThat(value.doubleValue()).isEqualTo(100.0);
    }

    @Test
    void anomalyCase_returnsMeanPlusAnomalyMean_whenStdDevIsZero() {
        AnomalyAwareGaussNumberGenerator generator =
                new AnomalyAwareGaussNumberGenerator(new Random(), gaussProperties, anomalyRandomizer);
        when(anomalyRandomizer.isAnomaly()).thenReturn(true);

        BigDecimal value = generator.generateData();

        assertThat(value.doubleValue()).isEqualTo(300.0);
    }

    @Test
    void delegatesAnomalyDecision_toAnomalyRandomizer() {
        AnomalyAwareGaussNumberGenerator generator =
                new AnomalyAwareGaussNumberGenerator(new Random(), gaussProperties, anomalyRandomizer);
        when(anomalyRandomizer.isAnomaly()).thenReturn(false);

        generator.generateData();

        verify(anomalyRandomizer).isAnomaly();
    }

    @Test
    void normalCase_matchesExactValue_forASeededRandom() {
        gaussProperties.setStdDev(15);
        when(anomalyRandomizer.isAnomaly()).thenReturn(false);

        // two Random instances seeded identically produce the identical sequence,
        // so we can predict the exact noise value the generator will draw
        Random referenceRandom = new Random(42);
        double expectedNoise = referenceRandom.nextGaussian();
        double expected = 100 + 15 * expectedNoise;

        AnomalyAwareGaussNumberGenerator generator =
                new AnomalyAwareGaussNumberGenerator(new Random(42), gaussProperties, anomalyRandomizer);
        BigDecimal actual = generator.generateData();

        assertThat(actual.doubleValue()).isCloseTo(expected, offset(1e-9));
    }

    @Test
    void normalValues_clusterAroundMean_withRealisticStdDev() {
        gaussProperties.setStdDev(15);
        when(anomalyRandomizer.isAnomaly()).thenReturn(false);
        AnomalyAwareGaussNumberGenerator generator =
                new AnomalyAwareGaussNumberGenerator(new Random(), gaussProperties, anomalyRandomizer);

        int iterations = 1000;
        double sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += generator.generateData().doubleValue();
        }
        double average = sum / iterations;

        assertThat(average).isCloseTo(100.0, offset(3.0));
    }
}
