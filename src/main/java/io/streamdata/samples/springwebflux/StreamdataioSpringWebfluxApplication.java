/*
 * Copyright 2017 Streamdata.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.streamdata.samples.springwebflux;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.time.Duration;

@SpringBootApplication
public class StreamdataioSpringWebfluxApplication {

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication app = new SpringApplication(StreamdataioSpringWebfluxApplication.class);
		// prevent SpringBoot from starting a web server
		app.setWebApplicationType(WebApplicationType.NONE);
		app.run(args);

	}

	@Bean
	public CommandLineRunner myCommandLineRunner() {
		return args -> {

			// Add a block here because CommandLineRunner returns after the execution of the code
			// ... and make the code run 1 day.
			Mono.just("That's the end!")
				.delayElement(Duration.ofDays(1))
				.block();
		};
	}
}