package org.example.cint_consumer.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class SlidingWindowZValueCalculatorTest {

    private static final double EPSILON = 1e-9;

    @Test
    void firstDataPoint_hasNoBaselineYet_returnsZero() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(100);
        BigDecimal zScore = calculator.calculateZScore(BigDecimal.TEN);

        assertThat(zScore.doubleValue()).isZero();
    }

    @Test
    void secondDataPoint_stillOnlyOnePriorPoint_returnsZero() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(100);
        calculator.calculateZScore(BigDecimal.TEN);
        BigDecimal zScore = calculator.calculateZScore(BigDecimal.valueOf(20));

        assertThat(zScore.doubleValue()).isZero();
    }

    @Test
    void thirdDataPoint_isEvaluatedAgainstFirstTwoPoints_notItself() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(100);
        calculator.calculateZScore(BigDecimal.valueOf(10));
        calculator.calculateZScore(BigDecimal.valueOf(20));

        BigDecimal zScore = calculator.calculateZScore(BigDecimal.valueOf(100));

        assertThat(zScore.doubleValue()).isCloseTo(17.0, offset(EPSILON));
    }

    @Test
    void identicalValues_stdDevIsZero_doesNotDivideByZero() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(10);

        for (int i = 0; i < 5; i++) {
            calculator.calculateZScore(BigDecimal.valueOf(42));
        }
        BigDecimal zScore = calculator.calculateZScore(BigDecimal.valueOf(42));

        assertThat(zScore.doubleValue()).isZero();
    }

    @Test
    void obviousOutlier_producesLargeZScore() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(50);

        for (int i = 0; i < 30; i++) {
            calculator.calculateZScore(BigDecimal.valueOf(100));
        }

        calculator.calculateZScore(BigDecimal.valueOf(101));
        calculator.calculateZScore(BigDecimal.valueOf(99));

        BigDecimal zScore = calculator.calculateZScore(BigDecimal.valueOf(1000));

        assertThat(zScore.doubleValue()).isGreaterThan(3.0);
    }

    @Test
    void belowMeanValue_producesPositiveZScoreMagnitude() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(100);
        calculator.calculateZScore(BigDecimal.valueOf(10));
        calculator.calculateZScore(BigDecimal.valueOf(20));

        BigDecimal zScore = calculator.calculateZScore(BigDecimal.valueOf(-70));

        assertThat(zScore.doubleValue()).isCloseTo(17.0, offset(EPSILON));
    }

    @Test
    void windowSizeBelowTwo_isRejectedAtConstruction() {
        assertThatThrownBy(() -> new SlidingWindowZValueCalculator(1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new SlidingWindowZValueCalculator(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeInputData_isHandledCorrectly() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(100);
        calculator.calculateZScore(BigDecimal.valueOf(-10));
        calculator.calculateZScore(BigDecimal.valueOf(-20));

        BigDecimal zScore = calculator.calculateZScore(BigDecimal.valueOf(-100));

        assertThat(zScore.doubleValue()).isCloseTo(17.0, offset(EPSILON));
    }

    @Test
    void windowEviction_dropsOldestPoint_onceMaxSizeReached() {
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(3);

        double[] values = {10, 12, 11, 500};
        for (double v : values) {
            calculator.calculateZScore(BigDecimal.valueOf(v));
        }

        BigDecimal actual = calculator.calculateZScore(BigDecimal.valueOf(50));
        double expected = naiveZScoreAgainst(new double[]{12, 11, 500}, 50);

        assertThat(actual.doubleValue()).isCloseTo(expected, offset(1e-6));
    }

    @Test
    void differentialTest_matchesNaiveRollingWindowRecomputation() {
        int maxSize = 10;
        ZValueCalculator calculator = new SlidingWindowZValueCalculator(maxSize);

        Random random = new Random(42);
        LinkedList<Double> referenceWindow = new LinkedList<>();

        for (int i = 0; i < 200; i++) {
            double next = 100 + random.nextGaussian() * 15;

            double expected = referenceWindow.size() >= 2
                    ? naiveZScoreAgainst(toArray(referenceWindow), next)
                    : 0.0;

            BigDecimal actual = calculator.calculateZScore(BigDecimal.valueOf(next));

            assertThat(actual.doubleValue())
                    .as("iteration %d, value %f", i, next)
                    .isCloseTo(expected, offset(1e-6));

            referenceWindow.addFirst(next);
            if (referenceWindow.size() > maxSize) {
                referenceWindow.removeLast();
            }
        }
    }

    private static double[] toArray(LinkedList<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private static double naiveZScoreAgainst(double[] window, double value) {
        double mean = 0;
        for (double v : window) {
            mean += v;
        }
        mean /= window.length;

        double variance = 0;
        for (double v : window) {
            variance += Math.pow(v - mean, 2);
        }
        variance /= window.length;
        double stdDev = Math.sqrt(variance);

        return stdDev == 0 ? 0.0 : Math.abs(value - mean) / stdDev;
    }
}
