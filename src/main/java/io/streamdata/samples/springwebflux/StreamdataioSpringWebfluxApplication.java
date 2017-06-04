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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
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

            // use of a transformer to apply the patches
            events.as(new PatchTransformer())
                  // Subscribe to the flux with a consumer that applies patches
                  .subscribe(System.out::println,
                             Throwable::printStackTrace);
            ;

            // Add a block here because CommandLineRunner returns after the execution of the code
            // ... and make the code run 1 day.
            Mono.just("That's the end!")
                .delayElement(Duration.ofDays(1))
                .block();
        };
    }

    static class PatchTransformer implements Function<Flux<ServerSentEvent<JsonNode>>, Flux<JsonNode>> {

        @Override
        public Flux<JsonNode> apply(final Flux<ServerSentEvent<JsonNode>> aFlux) {
            return aFlux.filter(evt -> evt.data().isPresent())
                        .filter(evt -> evt.event()
                                          .map(evtType -> "data".equals(evtType)
                                              || "patch".equals(evtType)
                                              || "error".equals(evtType))
                                          .orElse(FALSE))
                        .map(new Function<ServerSentEvent<JsonNode>, JsonNode>() {
                            private JsonNode current;

                            @Override
                            public JsonNode apply(final ServerSentEvent<JsonNode> aEvent) {
                                String type = aEvent.event().get();

                                switch (type) {
                                    case "data":
                                        current = aEvent.data().get();
                                        break;

                                    case "patch":
                                        current = JsonPatch.apply(aEvent.data().get(), current);
                                        break;

                                    case "error":
                                        aEvent.data()
                                              .ifPresent(data -> {
                                                  throw new RuntimeException("received an error! " + data);
                                              });
                                        break;

                                    default:
                                        throw new IllegalArgumentException("Unknown type: " + type);
                                }

                                return current;
                            }
                        });
        }
    }

}