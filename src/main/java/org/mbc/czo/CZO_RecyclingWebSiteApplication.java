package org.mbc.czo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableKafka
@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class CZO_RecyclingWebSiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(CZO_RecyclingWebSiteApplication.class, args);
    }

}
