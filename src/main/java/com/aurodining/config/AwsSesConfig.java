package com.aurodining.config;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "email.provider", havingValue = "ses")
public class AwsSesConfig {

    @Bean(destroyMethod = "shutdown")
    public AmazonSimpleEmailService sesClient(
            @Value("${aws.region:us-east-1}") String region) {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(region)
                .build();
    }
}
