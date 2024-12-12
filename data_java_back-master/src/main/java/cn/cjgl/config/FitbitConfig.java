package cn.cjgl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "fitbit")
public class FitbitConfig {
    String clientId;
    String clientSecret;
    String redirectUrl;
}
