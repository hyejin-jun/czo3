package org.mbc.czo.function.apiSecurity.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*application.properties에서 값 받아오기가능*/
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String accessSecret;
    private long accessValidityMs;
    private String refreshSecret;
    private long refreshValidityMs;
}
