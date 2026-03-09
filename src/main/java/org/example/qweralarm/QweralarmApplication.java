package org.example.qweralarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QweralarmApplication {

    public static void main(String[] args) {
        SpringApplication.run(QweralarmApplication.class, args);
    }

}
