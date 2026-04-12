package ca.mcgill.ecse321.ecse321project03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Ecse321project03Application {

	public static void main(String[] args) {
		SpringApplication.run(Ecse321project03Application.class, args);
	}

}
