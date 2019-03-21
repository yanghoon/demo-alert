package zcp.demo.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DemoAlertApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoAlertApplication.class, args);
	}

}
