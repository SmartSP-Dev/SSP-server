package group4.opensource_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication()
public class OpensourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpensourceServerApplication.class, args);
    }

}
