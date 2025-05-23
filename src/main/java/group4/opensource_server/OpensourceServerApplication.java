package group4.opensource_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpensourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpensourceServerApplication.class, args);
    }

}
