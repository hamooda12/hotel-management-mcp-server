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

    @Tool(description = "Search and browse the hotel catalog. You can filter by city, hotel name, description, and creation date range, and control pagination and sorting. Use only the filters that are needed.")
    public Map<String, Object> searchHotels(
            @ToolParam(description = "City to filter hotels by. Optional.", required = false) String city,
            @ToolParam(description = "Text that should be contained in the hotel name. Optional.", required = false) String nameContains,
            @ToolParam(description = "Text to search for in the hotel description. Optional.", required = false) String description,
            @ToolParam(description = "Return hotels created before this date, in YYYY-MM-DD format. Optional.", required = false) String before,
            @ToolParam(description = "Return hotels created after this date, in YYYY-MM-DD format. Optional.", required = false) String after,
            @ToolParam(description = "Zero-based page number. Optional; defaults to 0.", required = false) Integer page,
            @ToolParam(description = "Number of hotels per page. Optional; defaults to the backend default.", required = false) Integer size,
            @ToolParam(description = "Sort expression such as 'id,desc', 'name,asc', or 'city,asc'. Optional.", required = false) String sort) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/hotels");

        addQueryParam(builder, "city", city);
        addQueryParam(builder, "nameContains", nameContains);
        addQueryParam(builder, "description", description);
        addQueryParam(builder, "before", before);
        addQueryParam(builder, "after", after);

        if (page != null) {
            builder.queryParam("page", page);
        }
        if (size != null) {
            builder.queryParam("size", size);
        }
        addQueryParam(builder, "sort", sort);

        return hotelBackendRestClient.get()
                .uri(builder.toUriString())
                .retrieve()
                .body(Map.class);
    }

    private void addQueryParam(UriComponentsBuilder builder, String name, String value) {
        if (value != null && !value.isBlank()) {
            builder.queryParam(name, value);
        }
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
        if (description != null && !description.isBlank()) {
            request.put("description", description);
        }

        return authenticatedRestClient.post(
                "/api/hotels",
                request
        );
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
        if (amenities != null && !amenities.isBlank()) {
            request.put("amenities", amenities);
        }

        return authenticatedRestClient.post(
                "/api/room-types/hotel/" + hotelId,
                request
        );
    }

    @Tool(description = "Upload or replace the main image of a hotel using a publicly accessible HTTPS image URL. The MCP server downloads the image and uploads it to the hotel backend as multipart form data.")
    public Map<String, Object> uploadHotelImage(
            @ToolParam(description = "Hotel ID whose main image should be uploaded.") Long hotelId,
            @ToolParam(description = "Public HTTPS URL of the image to use for the hotel.") String imageUrl,
            @ToolParam(description = "Filename including a supported image extension such as .jpg, .jpeg, .png, .webp, or .gif. Optional; defaults to image.jpg.", required = false) String fileName) {

        return authenticatedRestClient.uploadImageFromUrl(
                "/api/hotels/" + hotelId + "/image",
                imageUrl,
                fileName
        );
    }

    @Tool(description = "Upload or replace the main image of a room type using a publicly accessible HTTPS image URL. The MCP server downloads the image and uploads it to the hotel backend as multipart form data.")
    public Map<String, Object> uploadRoomTypeImage(
            @ToolParam(description = "Room type ID whose main image should be uploaded.") Long roomTypeId,
            @ToolParam(description = "Public HTTPS URL of the image to use for the room type.") String imageUrl,
            @ToolParam(description = "Filename including a supported image extension such as .jpg, .jpeg, .png, .webp, or .gif. Optional; defaults to image.jpg.", required = false) String fileName) {

        return authenticatedRestClient.uploadImageFromUrl(
                "/api/room-types/" + roomTypeId + "/image",
                imageUrl,
                fileName
        );
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

    @Tool(description = "Get detailed information about a booking by its ID. The authenticated user can access their own booking, while administrators and managers can access any booking.")
    public Map<String, Object> getBooking(
            @ToolParam(description = "The unique ID of the booking.") Long id) {

        String uri = "/api/bookings/" + id;

        return authenticatedRestClient.get(uri);
    }

    @Tool(description = "Create a payment intent for a pending hotel booking. The payment is initially created with INITIATED status.")
    public Map<String, Object> createPaymentIntent(
            @ToolParam(description = "The unique ID of the booking to create a payment intent for.") Long bookingId) {

        Map<String, Object> request = Map.of(
                "bookingId", bookingId
        );

        return authenticatedRestClient.post(
                "/api/payments/intent",
                request
        );
    }

    @Tool(description = "Simulate a payment outcome for an initiated hotel payment. SUCCESS confirms the booking; FAILED leaves the booking pending.")
    public Map<String, Object> simulatePayment(
            @ToolParam(description = "The unique ID of the payment.") Long paymentId,
            @ToolParam(description = "Payment outcome. Must be SUCCESS or FAILED.") String outcome) {

        Map<String, Object> request = Map.of(
                "outcome", outcome
        );

        return authenticatedRestClient.post(
                "/api/payments/" + paymentId + "/simulate",
                request
        );
    }

    @Tool(description = "Cancel a hotel booking for the authenticated user. The backend applies its cancellation eligibility rules.")
    public Map<String, Object> cancelBooking(
            @ToolParam(description = "The unique ID of the booking to cancel.") Long bookingId) {

        return authenticatedRestClient.patch(
                "/api/bookings/" + bookingId + "/cancel"
        );
    }

    @Tool(description = "Get detailed information about a payment by its ID. The authenticated user can access their own payment, while administrators and managers can access any payment.")
    public Map<String, Object> getPayment(
            @ToolParam(description = "The unique ID of the payment.") Long paymentId) {

        return authenticatedRestClient.get(
                "/api/payments/" + paymentId
        );
    }

    @Tool(description = "Get the booking history of the authenticated guest. Returns the guest's bookings across their booking history.")
    public List<Map<String, Object>> getBookingHistory() {

        return authenticatedRestClient.getList(
                "/api/bookings/guest-history"
        );
    }
}