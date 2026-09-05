package com.hamooda.hotelmcp.chat;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class ChatManagementTool {

    private final RestClient hotelBackendRestClient;

    public ChatManagementTool(RestClient hotelBackendRestClient) {
        this.hotelBackendRestClient = hotelBackendRestClient;
    }

    @Tool(
            name = "delete_chat",
            description = "Permanently delete the authenticated user's persisted AI chat history for the supplied conversation ID. Use only when the user explicitly asks to delete, clear, erase, or forget the current chat. This deletes conversation history, not long-term user memory."
    )
    public Map<String, Object> deleteChat(
            @ToolParam(description = "The current client conversation ID. Use the conversation ID supplied in the assistant request context; never invent one.") String conversationId) {

        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId is required");
        }

        hotelBackendRestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/AI/history")
                        .queryParam("conversationId", conversationId)
                        .build())
                .retrieve()
                .toBodilessEntity();

        return Map.of(
                "success", true,
                "conversationId", conversationId,
                "message", "The chat history was deleted. Long-term memory was not changed."
        );
    }
}
