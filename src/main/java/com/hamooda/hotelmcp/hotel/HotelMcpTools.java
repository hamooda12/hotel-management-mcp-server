package com.hamooda.hotelmcp.hotel;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class HotelMcpTools {

    @Tool(description = "Search the hotel catalog using optional city and hotel-name filters. Returns a paginated list of matching hotels.")
    public String searchHotels(
            @ToolParam(description = "City to filter hotels by. Optional.", required = false)
            String city,
            @ToolParam(description = "Part of the hotel name to search for. Optional.", required = false)
            String nameContains
    ) {
        throw new UnsupportedOperationException("Tool implementation will be added next");
    }
}
