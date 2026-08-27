package com.hamooda.hotelmcp.hotel;

import com.hamooda.hotelmcp.auth.AuthenticatedRestClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class HotelDeletionTool {

    private final AuthenticatedRestClient authenticatedRestClient;

    public HotelDeletionTool(AuthenticatedRestClient authenticatedRestClient) {
        this.authenticatedRestClient = authenticatedRestClient;
    }

    @Tool(description = "Delete a hotel by ID. This permanently deletes the hotel. Requires ADMIN authorization. Only use when the user explicitly asks to delete the hotel.")
    public String deleteHotel(
            @ToolParam(description = "The unique ID of the hotel to permanently delete.") Long hotelId) {
        authenticatedRestClient.delete("/api/hotels/" + hotelId);
        return "Hotel " + hotelId + " deleted successfully.";
    }

    @Tool(description = "Delete a room type by ID. This permanently deletes the room type. Requires ADMIN authorization. Only use when the user explicitly asks to delete the room type.")
    public String deleteRoomType(
            @ToolParam(description = "The unique ID of the room type to permanently delete.") Long roomTypeId) {
        authenticatedRestClient.delete("/api/room-types/" + roomTypeId);
        return "Room type " + roomTypeId + " deleted successfully.";
    }
}
