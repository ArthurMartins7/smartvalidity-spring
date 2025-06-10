package br.com.smartvalidity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartvalidityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartvalidityApplication.class, args);
    }
    
}
