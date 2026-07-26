package com.rera.auditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AiReraAuditorApplication {
    public static void main(String[] args) {

        
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		

        SpringApplication.run(AiReraAuditorApplication.class, args);
    }
}
