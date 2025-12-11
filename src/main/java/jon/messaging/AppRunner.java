package jon.messaging;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRabbit
@EnableAsync
@EnableScheduling
public class AppRunner {

	public static void main(String[] args) {
		SpringApplication.run(AppRunner.class, args);
	}

}
