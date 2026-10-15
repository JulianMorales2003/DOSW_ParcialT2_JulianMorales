package edu.dosw.parcial.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "edu.dosw.parcial")
public class ParcialApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParcialApplication.class, args);
    }
}