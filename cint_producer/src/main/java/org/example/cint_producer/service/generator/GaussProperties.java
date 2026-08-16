package org.example.cint_producer.service.generator;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "gauss")
@Validated
@Data
public class GaussProperties {
    private double mean;
    private double stdDev;

    @DecimalMin(value = "0.0", message = "gauss.anomalyChance must be a probability between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "gauss.anomalyChance must be a probability between 0.0 and 1.0")
    private double anomalyChance;

    private double anomalyMean;
}
