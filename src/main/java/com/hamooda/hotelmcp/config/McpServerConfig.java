package com.hamooda.hotelmcp.config;

import com.hamooda.hotelmcp.auth.RegisterUserTool;
import com.hamooda.hotelmcp.hotel.HotelDeletionTool;
import com.hamooda.hotelmcp.hotel.HotelSearchTool;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig {

    @Bean
    ToolCallbackProvider hotelTools(
            HotelSearchTool hotelSearchTool,
            RegisterUserTool registerUserTool,
            HotelDeletionTool hotelDeletionTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(hotelSearchTool, registerUserTool, hotelDeletionTool)
                .build();
    }
    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> hotelPrompts() {

        var hotelSearchPrompt = new McpSchema.Prompt(
                "hotelSearch",
                "A prompt to search for hotels",
                List.of(
                        new McpSchema.PromptArgument(
                                "destination",
                                "The destination to search for",
                                true
                        )
                )
        );

        var hotelSearchPromptSpec =
                new McpServerFeatures.SyncPromptSpecification(
                        hotelSearchPrompt,
                        (exchange, getPromptRequest) -> {

                            String destination =
                                    (String) getPromptRequest.arguments()
                                            .get("destination");

                            var userMessage = new McpSchema.PromptMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent(
                                            String.format(
                                                    "Find available hotels in %s",
                                                    destination
                                            )
                                    )
                            );

                            return new McpSchema.GetPromptResult(
                                    String.format(
                                            "A prompt to search for hotels in %s",
                                            destination
                                    ),
                                    List.of(userMessage)
                            );
                        }
                );

        return List.of(hotelSearchPromptSpec);
    }
}
