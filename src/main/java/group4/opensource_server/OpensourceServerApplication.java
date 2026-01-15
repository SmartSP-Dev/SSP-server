package group4.opensource_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(
    basePackages = "group4.opensource_server",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "group4\\.opensource_server\\.OCR\\..*"
    )
)
public class OpensourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpensourceServerApplication.class, args);
    }

}
