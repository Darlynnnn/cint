package org.example.cint_consumer.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;

@Component
public class SlidingWindowZValueCalculator implements ZValueCalculator {

    private static final MathContext MC = MathContext.DECIMAL64;

    private final LinkedList<BigDecimal> window = new LinkedList<>();
    private final int maxSize;

    private BigDecimal mean = new BigDecimal(0);
    private BigDecimal m_square = new BigDecimal(0);

    public SlidingWindowZValueCalculator(@Value("${cint.window.size}") int maxSize) {
        if (maxSize < 2) {
            throw new IllegalArgumentException(
                    "cint.window.size must be at least 2 (a window of size " + maxSize
                            + " can never produce a variance); got: " + maxSize);
        }
        this.maxSize = maxSize;
    }

    @Override
    public BigDecimal calculateZScore(BigDecimal value) {
        BigDecimal zScore = BigDecimal.ZERO;

        if (window.size() >= 2) {
            BigDecimal variance = m_square.divide(new BigDecimal(window.size()), MC);
            BigDecimal stdDev = variance.sqrt(MC);

            if (stdDev.compareTo(BigDecimal.ZERO) != 0) {
                zScore = value.subtract(mean).abs().divide(stdDev, MC);
            }
        }

        if (window.size() < maxSize) {
            add(value);
        } else {
            removeLast();
            add(value);
        }

        return zScore;
    }

    private void removeLast() {
        BigDecimal last = window.removeLast();
        BigDecimal delta = last.subtract(mean);
        BigDecimal dividor = delta.divide(new BigDecimal(window.size()), MC);
        mean = mean.subtract(dividor);
        BigDecimal delta2 = last.subtract(mean);
        m_square = m_square.subtract(delta.multiply(delta2));
    }

    private void add(BigDecimal value) {
        window.addFirst(value);
        BigDecimal delta = value.subtract(mean);
        BigDecimal dividor = delta.divide(new BigDecimal(window.size()), MC);
        mean = mean.add(dividor);
        BigDecimal delta2 = value.subtract(mean);
        m_square = m_square.add(delta.multiply(delta2));
    }
}
