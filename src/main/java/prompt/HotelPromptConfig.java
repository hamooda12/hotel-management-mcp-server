package prompt;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HotelPromptConfig {

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> hotelPrompts() {

        var hotelRecommendationPrompt =
                new McpServerFeatures.SyncPromptSpecification(

                        // Prompt definition
                        new McpSchema.Prompt(
                                "hotel-room-recommendation",
                                "Helps the user choose a suitable hotel room.",
                                List.of(
                                        new McpSchema.PromptArgument(
                                                "guests",
                                                "Number of guests.",
                                                true
                                        ),
                                        new McpSchema.PromptArgument(
                                                "budget",
                                                "Maximum budget per night.",
                                                true
                                        )
                                )
                        ),

                        // Prompt handler
                        (exchange, request) -> {

                            var guests =
                                    request.arguments().get("guests");

                            var budget =
                                    request.arguments().get("budget");

                            var message =
                                    """
                                    Help the user choose a suitable hotel room.

                                    Number of guests: %s
                                    Maximum budget per night: %s

                                    Consider the available hotel room types
                                    and recommend suitable options.
                                    """.formatted(guests, budget);

                            return new McpSchema.GetPromptResult(
                                    "Hotel room recommendation prompt",
                                    List.of(
                                            new McpSchema.PromptMessage(
                                                    McpSchema.Role.USER,
                                                    new McpSchema.TextContent(message)
                                            )
                                    )
                            );
                        }
                );

        return List.of(hotelRecommendationPrompt);
    }
}