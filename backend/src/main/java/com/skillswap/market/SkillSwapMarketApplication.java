package com.skillswap.market;

import com.skillswap.market.security.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SkillSwapMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillSwapMarketApplication.class, args);
    }
}
