package com.hamooda.hotelmcp.config;

import com.hamooda.hotelmcp.hotel.HotelSearchTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    ToolCallback[] hotelTools(HotelSearchTool hotelSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(hotelSearchTool)
                .build()
                .getToolCallbacks();
    }
}
