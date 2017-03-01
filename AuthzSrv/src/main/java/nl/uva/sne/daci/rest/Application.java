package nl.uva.sne.daci.rest;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@SpringBootApplication
//@EnableAutoConfiguration
//@ComponentScan("nl.uva.sne.daci.rest")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}