package it.unipi.lsmd.restory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResToryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResToryApplication.class, args);
        System.out.println("Inzioooo...");
	}

}
