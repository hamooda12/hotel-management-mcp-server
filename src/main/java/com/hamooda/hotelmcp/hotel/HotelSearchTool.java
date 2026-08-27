package com.hamooda.hotelmcp.hotel;

import com.hamooda.hotelmcp.auth.AuthenticatedRestClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class HotelSearchTool {

    private final RestClient hotelBackendRestClient;
    private final AuthenticatedRestClient authenticatedRestClient;

    public HotelSearchTool(
            RestClient hotelBackendRestClient,
            AuthenticatedRestClient authenticatedRestClient) {

        this.hotelBackendRestClient = hotelBackendRestClient;
        this.authenticatedRestClient = authenticatedRestClient;
    }

    @Tool(description = "Search the hotel catalog using optional city and hotel-name filters. Returns a paginated list of matching hotels.")
    public Map<String, Object> searchHotels(
            @ToolParam(description = "City to filter hotels by. Optional.", required = false) String city,
            @ToolParam(description = "Text that should be contained in the hotel name. Optional.", required = false) String nameContains) {

        String uri = UriComponentsBuilder.fromPath("/api/hotels")
                .queryParamIfPresent(
                        "city",
                        java.util.Optional.ofNullable(city)
                                .filter(value -> !value.isBlank())
                )
                .queryParamIfPresent(
                        "nameContains",
                        java.util.Optional.ofNullable(nameContains)
                                .filter(value -> !value.isBlank())
                )
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

    @Tool(description = "Check room availability and total price for a hotel room type for specific dates and number of guests.")
    public Map<String, Object> checkAvailability(
            @ToolParam(description = "The unique ID of the hotel.") Long hotelId,
            @ToolParam(description = "The unique ID of the room type.") Long roomTypeId,
            @ToolParam(description = "Check-in date in YYYY-MM-DD format.") String checkinDate,
            @ToolParam(description = "Check-out date in YYYY-MM-DD format.") String checkoutDate,
            @ToolParam(description = "Number of guests.") Integer guests) {

        Map<String, Object> request = Map.of(
                "hotelId", hotelId,
                "roomTypeId", roomTypeId,
                "checkinDate", checkinDate,
                "checkoutDate", checkoutDate,
                "guests", guests
        );

        return hotelBackendRestClient.post()
                .uri("/api/availability/check")
                .body(request)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Create a hotel booking for the authenticated user.")
    public Map<String, Object> createBooking(
            @ToolParam(description = "Guest email for the booking.") String guestEmail,
            @ToolParam(description = "The unique ID of the room type.") Long roomTypeId,
            @ToolParam(description = "Check-in date in YYYY-MM-DD format.") String checkIn,
            @ToolParam(description = "Check-out date in YYYY-MM-DD format.") String checkOut,
            @ToolParam(description = "Number of guests.") Integer guests) {

        Map<String, Object> request = Map.of(
                "guestEmail", guestEmail,
                "roomTypeId", roomTypeId,
                "checkIn", checkIn,
                "checkOut", checkOut,
                "guests", guests
        );

        return authenticatedRestClient.post(
                "/api/bookings",
                request
        );
    }
}