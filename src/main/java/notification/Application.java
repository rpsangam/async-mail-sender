package notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Application.class);

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
	}
}
