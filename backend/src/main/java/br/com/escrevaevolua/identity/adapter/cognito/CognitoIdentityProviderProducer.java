package br.com.escrevaevolua.identity.adapter.cognito;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoIdentityProviderProducer {

    @Bean(destroyMethod = "close")
    CognitoIdentityProviderClient cognitoIdentityProviderClient(
            @Value("${identity.cognito.region:us-east-1}") String region) {
        return CognitoIdentityProviderClient.builder().region(Region.of(region)).build();
    }
}
