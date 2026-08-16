package org.example.cint_producer.service.generator.impl;

import lombok.RequiredArgsConstructor;
import org.example.cint_producer.service.generator.AnomalyRandomizer;
import org.example.cint_producer.service.generator.GaussProperties;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AnomalyRandomizerImpl implements AnomalyRandomizer {

    private final GaussProperties gaussProperties;
    private final Random random;

    @Override
    public boolean isAnomaly() {
        return random.nextDouble() < gaussProperties.getAnomalyChance();
    }
}
