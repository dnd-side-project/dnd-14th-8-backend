package com.dnd.moyeolak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MoyeolakApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoyeolakApplication.class, args);
    }

}
