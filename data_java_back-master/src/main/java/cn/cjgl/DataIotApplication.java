package cn.cjgl;

import cn.cjgl.config.FitbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FitbitConfig.class)
public class DataIotApplication {
	private static final Logger log = LoggerFactory.getLogger(DataIotApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DataIotApplication.class, args);
	}

	@Bean
	public CommandLineRunner dateCheckerRunner() {
		return new DateCheckerRunner();
	}

	@Component
	static class DateCheckerRunner implements CommandLineRunner {

		private static final String DEADLINE_DATE_STR = "2025-11-25"; // preset end date
		private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

		@Override
		public void run(String... args) throws Exception {
			LocalDate currentDate = LocalDate.now();
			LocalDate deadlineDate = LocalDate.parse(DEADLINE_DATE_STR, DATE_FORMATTER);

//			if (currentDate.isAfter(deadlineDate)) {
//				log.error("please apply fot enlongation data：" + currentDate + " > " + deadlineDate);
//				System.exit(1); // stop activating the application
//			} else {
//				log.warn("application start normally，current date：" + currentDate+",certification end date："+deadlineDate);
//			}
		}
	}


}
