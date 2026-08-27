package com.hamooda.hotelmcp.hotel;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class HotelSearchTool {

    private final RestClient hotelBackendRestClient;

    public HotelSearchTool(RestClient hotelBackendRestClient) {
        this.hotelBackendRestClient = hotelBackendRestClient;
    }

    @Tool(description = "Search the hotel catalog using optional city and hotel-name filters. Returns a paginated list of matching hotels.")
    public Map<String, Object> searchHotels(
            @ToolParam(description = "City to filter hotels by. Optional.", required = false) String city,
            @ToolParam(description = "Text that should be contained in the hotel name. Optional.", required = false) String nameContains) {

        String uri = UriComponentsBuilder.fromPath("/api/hotels")
                .queryParamIfPresent("city", java.util.Optional.ofNullable(city).filter(value -> !value.isBlank()))
                .queryParamIfPresent("nameContains", java.util.Optional.ofNullable(nameContains).filter(value -> !value.isBlank()))
                .toUriString();

        Map<String, Object> response = hotelBackendRestClient.get()
                .uri(uri)
                .retrieve()
                .body(Map.class);

        return response;
    }

    @Tool(description = "Get detailed information about a hotel by its ID, including its room types.")
    public Map<String, Object> getHotel(
            @ToolParam(description = "The unique ID of the hotel.") Long id) {

        String uri = "/api/hotels/" + id;

        return hotelBackendRestClient.get()
                .uri(uri)
                .retrieve()
                .body(Map.class);
    }
}
