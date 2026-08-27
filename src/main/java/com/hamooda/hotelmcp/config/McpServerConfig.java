package com.hamooda.hotelmcp.config;

import com.hamooda.hotelmcp.auth.RegisterUserTool;
import com.hamooda.hotelmcp.hotel.HotelSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    ToolCallbackProvider hotelTools(
            HotelSearchTool hotelSearchTool,
            RegisterUserTool registerUserTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(hotelSearchTool, registerUserTool)
                .build();
    }

}
