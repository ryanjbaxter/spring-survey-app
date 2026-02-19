package io.spring.sample.uijs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PollUIJSApplication {

    public static void main(String[] args) {
        SpringApplication.run(PollUIJSApplication.class, args);
    }

}
