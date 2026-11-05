package io.github.mtriet004.gscores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GScoresApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(GScoresApplication.class, args);
		if (context.getEnvironment().matchesProfiles("seed")) {
			context.close();
		}
	}

}
