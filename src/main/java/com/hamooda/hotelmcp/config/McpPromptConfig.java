package com.hamooda.hotelmcp.config;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpPromptConfig {

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> hotelPrompts() {

        var hotelBookingPrompt =
                new McpSchema.Prompt(
                        "hotelBookingAssistant",
                        "A prompt to help the user search for hotels and book a room.",
                        List.of(
                                new McpSchema.PromptArgument(
                                        "destination",
                                        "The destination where the user wants to stay.",
                                        true
                                ),
                                new McpSchema.PromptArgument(
                                        "guests",
                                        "Number of guests.",
                                        true
                                )
                        )
                );

        var promptSpecification =
                new McpServerFeatures.SyncPromptSpecification(
                        hotelBookingPrompt,

                        (exchange, request) -> {

                            String destination =
                                    (String) request.arguments().get("destination");

                            String guests =
                                    (String) request.arguments().get("guests");

                            var message =
                                    new PromptMessage(
                                            Role.USER,
                                            new TextContent(
                                                    """
                                                    Help me find a suitable hotel.

                                                    Destination: %s
                                                    Number of guests: %s

                                                    Search the available hotels and
                                                    recommend suitable options.
                                                    """.formatted(
                                                            destination,
                                                            guests
                                                    )
                                            )
                                    );

                            return new GetPromptResult(
                                    "Hotel booking assistant prompt.",
                                    List.of(message)
                            );
                        }
                );

        return List.of(promptSpecification);
    }
}