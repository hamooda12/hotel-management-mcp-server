package com.hamooda.hotelmcp.hotel;

import com.hamooda.hotelmcp.auth.AuthenticatedRestClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
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

    @Tool(description = "Find hotels in the hotel catalog. Use this tool FIRST whenever the user asks about a specific hotel by name. For a hotel-name question, pass the hotel's name or a distinctive part of it in nameContains, use page=0, and use a small size such as 5. The result contains hotel IDs needed by getHotel. This is the source of truth for hotel catalog data; do not use RAG instead.")
    public Map<String, Object> searchHotels(
            @ToolParam(description = "City to filter hotels by. Optional.", required = false) String city,
            @ToolParam(description = "Part of the hotel name to search for. REQUIRED when looking up a specific hotel by name.", required = false) String nameContains,
            @ToolParam(description = "Text to search for in the hotel description. Optional.", required = false) String description,
            @ToolParam(description = "Return hotels created before this date, in YYYY-MM-DD format. Optional.", required = false) String before,
            @ToolParam(description = "Return hotels created after this date, in YYYY-MM-DD format. Optional.", required = false) String after,
            @ToolParam(description = "Zero-based page number. For a specific hotel lookup, use 0. Optional.", required = false) Integer page,
            @ToolParam(description = "Number of hotels per page. For a specific hotel lookup, use a small value such as 5. Optional.", required = false) Integer size,
            @ToolParam(description = "Sort expression such as 'id,desc', 'name,asc', or 'city,asc'. Optional.", required = false) String sort) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/hotels");
        addQueryParam(builder, "city", city);
        addQueryParam(builder, "nameContains", nameContains);
        addQueryParam(builder, "description", description);
        addQueryParam(builder, "before", before);
        addQueryParam(builder, "after", after);

        if (page != null) builder.queryParam("page", page);
        if (size != null) builder.queryParam("size", size);
        addQueryParam(builder, "sort", sort);

        return hotelBackendRestClient.get()
                .uri(builder.toUriString())
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Get complete current details for one specific hotel, including its room types. ALWAYS use this after searchHotels when the user asks for the details, rooms, amenities, location, description, or other catalog information of a specific hotel. Do not answer those details from RAG when this tool is available.")
    public Map<String, Object> getHotel(
            @ToolParam(description = "The unique ID of the hotel returned by searchHotels.") Long id) {
        return hotelBackendRestClient.get()
                .uri("/api/hotels/" + id)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Create a new hotel in the hotel catalog. Requires hotel name, city, address, and manager email. Use this for hotel administration, not for booking a room.")
    public Map<String, Object> createHotel(
            @ToolParam(description = "Name of the hotel.") String name,
            @ToolParam(description = "City where the hotel is located.") String city,
            @ToolParam(description = "Hotel street address.") String address,
            @ToolParam(description = "Hotel description. Optional.", required = false) String description,
            @ToolParam(description = "Email address of the hotel manager.") String managerEmail) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("city", city);
        request.put("address", address);
        request.put("managerEmail", managerEmail);
        if (description != null && !description.isBlank()) request.put("description", description);
        return authenticatedRestClient.post("/api/hotels", request);
    }

    @Tool(description = "Update an existing hotel. Requires the hotel ID, name, city, address, manager email, and optional description. Requires ADMIN or MANAGER authorization.")
    public Map<String, Object> updateHotel(
            @ToolParam(description = "The unique ID of the hotel to update.") Long hotelId,
            @ToolParam(description = "Updated hotel name.") String name,
            @ToolParam(description = "Updated city.") String city,
            @ToolParam(description = "Updated hotel street address.") String address,
            @ToolParam(description = "Updated hotel description. Optional.", required = false) String description,
            @ToolParam(description = "Updated hotel manager email.") String managerEmail) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("city", city);
        request.put("address", address);
        request.put("managerEmail", managerEmail);
        if (description != null && !description.isBlank()) request.put("description", description);
        return authenticatedRestClient.put("/api/hotels/" + hotelId, request);
    }

    @Tool(description = "Create a new room type for a specific hotel. The room type defines the room name, guest capacity, base price, amenities, and number of physical rooms available.")
    public Map<String, Object> createRoomType(
            @ToolParam(description = "The unique ID of the hotel where this room type will be created.") Long hotelId,
            @ToolParam(description = "Room type name, such as Single, Double, Deluxe, or Suite.") String name,
            @ToolParam(description = "Maximum number of guests the room type can accommodate. Must be at least 1.") Integer capacity,
            @ToolParam(description = "Base price per room. Must be greater than 0.") BigDecimal basePrice,
            @ToolParam(description = "Comma-separated or human-readable list of room amenities. Optional.", required = false) String amenities,
            @ToolParam(description = "Number of physical rooms of this type available in the hotel. Must be at least 1.") Integer totalRooms) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("capacity", capacity);
        request.put("basePrice", basePrice);
        request.put("totalRooms", totalRooms);
        if (amenities != null && !amenities.isBlank()) request.put("amenities", amenities);
        return authenticatedRestClient.post("/api/room-types/hotel/" + hotelId, request);
    }

    @Tool(description = "Search and browse room types using optional filters for amenities, name, capacity, total rooms, price, pagination, and sorting.")
    public Map<String, Object> searchRoomTypes(
            @ToolParam(description = "Filter by room amenities. Optional.", required = false) String amenities,
            @ToolParam(description = "Text that should be contained in the room type name. Optional.", required = false) String nameContains,
            @ToolParam(description = "Minimum guest capacity. Optional.", required = false) Integer minCapacity,
            @ToolParam(description = "Maximum guest capacity. Optional.", required = false) Integer maxCapacity,
            @ToolParam(description = "Minimum number of physical rooms. Optional.", required = false) Integer minTotalRooms,
            @ToolParam(description = "Maximum number of physical rooms. Optional.", required = false) Integer maxTotalRooms,
            @ToolParam(description = "Minimum base price. Optional.", required = false) BigDecimal minPrice,
            @ToolParam(description = "Maximum base price. Optional.", required = false) BigDecimal maxPrice,
            @ToolParam(description = "Zero-based page number. Optional; defaults to 0.", required = false) Integer page,
            @ToolParam(description = "Number of room types per page. Optional; defaults to the backend default.", required = false) Integer size,
            @ToolParam(description = "Sort expression such as 'id,desc', 'name,asc', 'capacity,asc', or 'basePrice,asc'. Optional.", required = false) String sort) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/room-types");
        addQueryParam(builder, "amenities", amenities);
        addQueryParam(builder, "nameContains", nameContains);
        addQueryParam(builder, "minCapacity", minCapacity);
        addQueryParam(builder, "maxCapacity", maxCapacity);
        addQueryParam(builder, "minTotalRooms", minTotalRooms);
        addQueryParam(builder, "maxTotalRooms", maxTotalRooms);
        addQueryParam(builder, "minPrice", minPrice);
        addQueryParam(builder, "maxPrice", maxPrice);
        addQueryParam(builder, "page", page);
        addQueryParam(builder, "size", size);
        addQueryParam(builder, "sort", sort);
        return hotelBackendRestClient.get().uri(builder.toUriString()).retrieve().body(Map.class);
    }

    private void addQueryParam(UriComponentsBuilder builder, String name, Object value) {
        if (value != null && (!(value instanceof String) || !((String) value).isBlank())) {
            builder.queryParam(name, value);
        }
    }

    @Tool(description = "Get detailed information about a room type by its ID.")
    public Map<String, Object> getRoomType(
            @ToolParam(description = "The unique ID of the room type.") Long roomTypeId) {
        return hotelBackendRestClient.get().uri("/api/room-types/" + roomTypeId).retrieve().body(Map.class);
    }

    @Tool(description = "Get all room types belonging to a specific hotel.")
    public List<Map<String, Object>> getRoomTypesByHotel(
            @ToolParam(description = "The unique ID of the hotel.") Long hotelId) {
        return hotelBackendRestClient.get().uri("/api/room-types/hotel/" + hotelId).retrieve().body(List.class);
    }

    @Tool(description = "Update an existing room type. Requires the room type ID and the complete room type data. Requires ADMIN or MANAGER authorization.")
    public Map<String, Object> updateRoomType(
            @ToolParam(description = "The unique ID of the room type to update.") Long roomTypeId,
            @ToolParam(description = "Updated room type name.") String name,
            @ToolParam(description = "Updated maximum guest capacity. Must be at least 1.") Integer capacity,
            @ToolParam(description = "Updated base price. Must be greater than 0.") BigDecimal basePrice,
            @ToolParam(description = "Updated room amenities. Optional.", required = false) String amenities,
            @ToolParam(description = "Updated number of physical rooms. Must be at least 1.") Integer totalRooms) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("capacity", capacity);
        request.put("basePrice", basePrice);
        request.put("totalRooms", totalRooms);
        if (amenities != null && !amenities.isBlank()) request.put("amenities", amenities);
        return authenticatedRestClient.put("/api/room-types/" + roomTypeId, request);
    }

    @Tool(description = "Upload or replace the main image of a hotel using a publicly accessible HTTPS image URL.")
    public Map<String, Object> uploadHotelImage(
            @ToolParam(description = "Hotel ID whose main image should be uploaded.") Long hotelId,
            @ToolParam(description = "Public HTTPS URL of the image to use for the hotel.") String imageUrl,
            @ToolParam(description = "Filename including a supported image extension. Optional.", required = false) String fileName) {
        return authenticatedRestClient.uploadImageFromUrl("/api/hotels/" + hotelId + "/image", imageUrl, fileName);
    }

    @Tool(description = "Upload or replace the main image of a room type using a publicly accessible HTTPS image URL.")
    public Map<String, Object> uploadRoomTypeImage(
            @ToolParam(description = "Room type ID whose main image should be uploaded.") Long roomTypeId,
            @ToolParam(description = "Public HTTPS URL of the image to use for the room type.") String imageUrl,
            @ToolParam(description = "Filename including a supported image extension. Optional.", required = false) String fileName) {
        return authenticatedRestClient.uploadImageFromUrl("/api/room-types/" + roomTypeId + "/image", imageUrl, fileName);
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
                "guests", guests);
        return hotelBackendRestClient.post().uri("/api/availability/check").body(request).retrieve().body(Map.class);
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
                "guests", guests);
        return authenticatedRestClient.post("/api/bookings", request);
    }

    @Tool(description = "Get detailed information about a booking by its ID.")
    public Map<String, Object> getBooking(
            @ToolParam(description = "The unique ID of the booking.") Long id) {
        return authenticatedRestClient.get("/api/bookings/" + id);
    }

    @Tool(description = "Get all hotel bookings. This administrative tool is available only to users with ADMIN or MANAGER authorization.")
    public List<Map<String, Object>> getAllBookings() {
        return authenticatedRestClient.getList("/api/bookings");
    }

    @Tool(description = "Get all bookings associated with a specific room type. This administrative tool is available only to users with ADMIN or MANAGER authorization.")
    public List<Map<String, Object>> getBookingsForRoomType(
            @ToolParam(description = "The unique ID of the room type.") Long roomTypeId) {
        return authenticatedRestClient.getList("/api/bookings/room-types/" + roomTypeId);
    }

    @Tool(description = "Get upcoming bookings for the currently authenticated hotel manager. Requires MANAGER or ADMIN authorization.")
    public List<Map<String, Object>> getManagerUpcomingBookings() {
        return authenticatedRestClient.getList("/api/bookings/manager-upcoming");
    }
}
