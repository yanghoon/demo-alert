package zcp.demo.alert;

import java.util.HashSet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableScheduling
@SpringBootApplication
public class DemoAlertApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoAlertApplication.class, args);
	}

	@Configuration
	@EnableSwagger2
	public class SwaggerConfig {
		@Bean
		public Docket api() {
			/**
			 * https://springfox.github.io/springfox/docs/current/#gradle
			 */
			HashSet<String> proto = new HashSet<>();
			proto.add("http");
			return new Docket(DocumentationType.SWAGGER_2)
					.protocols(proto)
					.select()
						.apis(RequestHandlerSelectors.any())
						.paths(PathSelectors.ant("/load/**"))
					.build();
		}
	}
}
