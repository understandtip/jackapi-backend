package com.jackqiu.jackapi.jackapibackendsdk;

import com.jackqiu.jackapi.jackapibackendsdk.config.APIClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("api.client")
@Data
@ComponentScan
public class APIClientConfig {

    private String assessKey;

    private String secretKey;

    private String url;

    private String gatewayHost;

    @Bean
    public APIClient apiClient() {
        return new APIClient(assessKey, secretKey, url, gatewayHost);
    }
}
