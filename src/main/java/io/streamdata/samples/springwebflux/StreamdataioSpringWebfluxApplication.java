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

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonPatch;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.Boolean.FALSE;
import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.BodyExtractors.toFlux;

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
			String api = "http://stockmarket.streamdata.io/prices";
			String token = "[YOUR TOKEN HERE]";

			URI streamdataUri = new URI("https://streamdata.motwin.net/"
												+ api
												+ "?X-Sd-Token="
												+ token);

			// source: https://github.com/spring-projects/spring-framework/blob/v5.0.0.RC1/spring-webflux/src/test/java/org/springframework/web/reactive/result/method/annotation/SseIntegrationTests.java
			ResolvableType type = forClassWithGenerics(ServerSentEvent.class, JsonNode.class);

			// Create the web client and the flux of events
			WebClient client = WebClient.create();
			Flux<ServerSentEvent<JsonNode>> events =
					client.get()
						  .uri(streamdataUri)
						  .accept(TEXT_EVENT_STREAM)
						  .exchange()
						  .flatMapMany(response -> response.body(toFlux(type)));

			// use a handler to apply patches and generate JsonNodes
			events.handle(new BiConsumer<ServerSentEvent<JsonNode>, SynchronousSink<JsonNode>>() {
				private JsonNode current;

				@Override
				public void accept(final ServerSentEvent<JsonNode> aEvent,
								   final SynchronousSink<JsonNode> aSink) {
					aEvent.event()
						  .ifPresent(type -> {
							  switch (type) {
								  case "data":
									  aEvent.data()
											.ifPresent(data -> {
												current = data;
												aSink.next(current);
											});
									  break;

								  case "patch":
									  aEvent.data()
											.ifPresent(data -> {
												current = JsonPatch.apply(data, current);
												aSink.next(current);
											});
									  break;

								  case "error":
									  aEvent.data()
											.ifPresent(data ->
															   aSink.error(new RuntimeException(
																	   "received an error! " + data)));
									  break;

								  default:
									  throw new IllegalArgumentException("Unknown type: " + type);
							  }
						  });
				}
			})
			// Subscribe to the flux with a consumer that applies patches
			.subscribe(System.out::println,
					   Throwable::printStackTrace);

			// Add a block here because CommandLineRunner returns after the execution of the code
			// ... and make the code run 1 day.
			Mono.just("That's the end!")
				.delayElement(Duration.ofDays(1))
				.block();
		};
	}
}