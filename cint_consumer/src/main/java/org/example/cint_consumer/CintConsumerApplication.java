package org.example.cint_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CintConsumerApplication {

    static void main(String[] args) {
        SpringApplication.run(CintConsumerApplication.class, args);
    }

}
