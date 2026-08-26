# Hotel Management MCP Server

MCP server for the existing Hotel Management application.

## Architecture

```text
AI Client / MCP Host
        |
        | MCP / Streamable HTTP
        v
+---------------------------+
| Hotel Management MCP      |
| Spring Boot               |
|                           |
| MCP Tools                 |
+-------------+-------------+
              |
              | HTTP
              v
+---------------------------+
| Existing Hotel Backend    |
| Spring Boot Monolith      |
| /api/hotels               |
+---------------------------+
```

The MCP server is intentionally separated from the existing monolith. It acts as an AI-facing adapter over the real application's REST API rather than duplicating the hotel domain logic.

## Current status

- Spring Boot application initialized
- Spring AI MCP Server WebMVC starter configured
- Streamable HTTP MCP transport configured
- Existing backend URL is configurable through `HOTEL_BACKEND_BASE_URL`
- First tool: `search_hotels` is planned next

## Existing backend contract

The current backend exposes `GET /api/hotels` for browsing hotels with pagination and filters including `city`, `nameContains`, `before`, `after`, and `description`. cite-placeholder

The backend also allows unauthenticated GET access to hotel endpoints, which makes the first read-only MCP tool suitable for calling the existing API without introducing credentials into the MCP server. 

## Run

```bash
export HOTEL_BACKEND_BASE_URL=http://localhost:8080
./mvnw spring-boot:run
```

MCP server port: `8081`.
