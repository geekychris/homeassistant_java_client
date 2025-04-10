# Home Assistant Java API Client

A comprehensive Java client library for interacting with the [Home Assistant](https://www.home-assistant.io/) REST API. This library provides a simple, intuitive interface for Java applications to communicate with Home Assistant servers.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.homeassistant/homeassistant-api-client.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.homeassistant/homeassistant-api-client)
[![javadoc](https://javadoc.io/badge2/io.github.homeassistant/homeassistant-api-client/javadoc.svg)](https://javadoc.io/doc/io.github.homeassistant/homeassistant-api-client)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Features

- 🔒 Simple authentication using long-lived access tokens
- 🔄 Complete access to Home Assistant REST API endpoints
- 📊 Full support for states, events, and services
- 🧩 Type-safe models with Jackson integration
- ⚡ Both synchronous and asynchronous operations
- 🛡️ Comprehensive error handling
- 📘 Extensive documentation and examples

## Installation

Add the following dependency to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>io.github.homeassistant</groupId>
    <artifactId>homeassistant-api-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

For Gradle projects, add to your `build.gradle`:

```groovy
implementation 'io.github.homeassistant:homeassistant-api-client:1.0.0'
```

## Quick Start

Here's a simple example to get you started:

```java
import io.github.homeassistant.client.HomeAssistantClient;
import io.github.homeassistant.model.State;

// Create a client instance
HomeAssistantClient client = new HomeAssistantClient(
    "http://homeassistant.local:8123",
    "YOUR_LONG_LIVED_ACCESS_TOKEN"
);

// Get all entity states
List<State> states = client.getStates();
states.forEach(state -> {
    System.out.println(state.getEntityId() + ": " + state.getState());
});

// Get state for a specific entity
State lightState = client.getState("light.living_room");
System.out.println("Living room light is: " + lightState.getState());

// Turn on a light
client.callService("light", "turn_on", 
    objectMapper.createObjectNode()
        .put("entity_id", "light.living_room")
        .put("brightness", 255)
);
```

## Authentication Setup

### Obtaining a Long-Lived Access Token

1. Open your Home Assistant instance in a web browser
2. Navigate to your profile (click on your username in the sidebar)
3. Scroll down to the "Long-Lived Access Tokens" section
4. Click "Create Token", give it a name, and copy the generated token

### Using the Token in the API Client

```java
// Create authentication manager directly
AuthenticationManager authManager = new AuthenticationManager("YOUR_LONG_LIVED_ACCESS_TOKEN");

// Or pass the token to the client constructor
HomeAssistantClient client = new HomeAssistantClient(
    "http://homeassistant.local:8123",
    "YOUR_LONG_LIVED_ACCESS_TOKEN"
);
```

## API Documentation

### State Operations

States represent the current status of entities in Home Assistant.

```java
// Get all states
List<State> allStates = client.getStates();

// Get state for a specific entity
State state = client.getState("sensor.temperature");
System.out.println("Temperature: " + state.getState() + "°C");

// Access entity attributes
JsonNode attributes = state.getAttributes();
if (attributes.has("unit_of_measurement")) {
    String unit = attributes.get("unit_of_measurement").asText();
    System.out.println("Unit: " + unit);
}

// Update a state
State newState = new State();
newState.setEntityId("input_boolean.test");
newState.setState("on");
client.setState(newState);
```

### Event Operations

Events allow you to listen for or trigger events in Home Assistant.

```java
// Get all available event types
List<String> eventTypes = client.getEvents();
System.out.println("Available events: " + eventTypes);

// Fire a custom event
ObjectNode eventData = objectMapper.createObjectNode();
eventData.put("some_data", "some_value");
client.fireEvent("my_custom_event", eventData);
```

### Service Operations

Services allow you to control devices and trigger automations.

```java
// Get all available services
Map<String, Map<String, Service>> allServices = client.getServices();

// Get services for a specific domain
Map<String, Service> lightServices = client.getDomainServices("light");
lightServices.forEach((name, service) -> {
    System.out.println("Service: " + name);
    System.out.println("Description: " + service.getDescription());
});

// Call a service
ObjectNode serviceData = objectMapper.createObjectNode();
serviceData.put("entity_id", "media_player.living_room");
serviceData.put("volume_level", 0.5);
client.callService("media_player", "volume_set", serviceData);
```

## Configuration Options

The `HomeAssistantClient` can be configured with the following options:

```java
// Basic configuration
HomeAssistantClient client = new HomeAssistantClient(
    "http://homeassistant.local:8123",  // Home Assistant base URL
    "YOUR_LONG_LIVED_ACCESS_TOKEN"      // Authentication token
);

// Configure with custom timeouts
HomeAssistantClient client = HomeAssistantClient.builder()
    .baseUrl("http://homeassistant.local:8123")
    .accessToken("YOUR_LONG_LIVED_ACCESS_TOKEN")
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .build();
```

## Error Handling

The client uses a custom exception hierarchy for better error handling:

```java
try {
    State state = client.getState("non_existent_entity");
} catch (HomeAssistantNotFoundException e) {
    System.err.println("Entity not found: " + e.getMessage());
} catch (HomeAssistantAuthenticationException e) {
    System.err.println("Authentication error: " + e.getMessage());
} catch (HomeAssistantCommunicationException e) {
    System.err.println("Communication error: " + e.getMessage());
} catch (HomeAssistantException e) {
    System.err.println("General error: " + e.getMessage());
}
```

Common exceptions:

- `HomeAssistantAuthenticationException`: Token-related errors (invalid, expired)
- `HomeAssistantNotFoundException`: Entity/service/resource not found
- `HomeAssistantCommunicationException`: Network or communication problems
- `HomeAssistantParsingException`: JSON parsing errors
- `HomeAssistantException`: Base exception class for any other errors

## Version Compatibility

| Library Version | Home Assistant Version | Java Version |
|-----------------|------------------------|--------------|
| 1.0.0           | 2023.9.0+              | Java 11+     |

## Best Practices

### Rate Limiting

Home Assistant doesn't enforce strict rate limits, but be considerate:

```java
// Bad: Polling too frequently
while (true) {
    State state = client.getState("binary_sensor.motion");
    Thread.sleep(100); // 10 times per second!
}

// Better: Reduce polling frequency
while (true) {
    State state = client.getState("binary_sensor.motion");
    Thread.sleep(5000); // Every 5 seconds
}

// Best: Use events API for real-time updates (when implemented)
```

### Error Resilience

Always handle errors properly and implement retries with backoff:

```java
int retries = 3;
int backoffMs = 1000;

for (int i = 0; i < retries; i++) {
    try {
        State state = client.getState("climate.living_room");
        // Success, break out of retry loop
        break;
    } catch (HomeAssistantCommunicationException e) {
        // Network error, try again after backoff
        if (i < retries - 1) {
            Thread.sleep(backoffMs * (i + 1));
        } else {
            throw e; // Rethrow after all retries
        }
    }
}
```

### Resource Management

Always close resources properly when using asynchronous operations:

```java
CompletableFuture<State> future = client.getStateAsync("sensor.temperature");
try {
    State state = future.get(10, TimeUnit.SECONDS);
    // Process state...
} catch (TimeoutException e) {
    future.cancel(true); // Cancel the operation if it times out
    throw new HomeAssistantCommunicationException("Request timed out", e);
} catch (Exception e) {
    // Handle other exceptions
}
```

## Contributing

Contributions are welcome! Here's how you can help:

1. **Fork the repository**: Create your own fork of the project
2. **Create a branch**: `git checkout -b feature/amazing-feature`
3. **Make changes**: Implement your feature or fix
4. **Add tests**: Ensure your code is properly tested
5. **Run checks**: `mvn verify` to run tests and checks
6. **Commit changes**: `git commit -m 'Add amazing feature'`
7. **Push to branch**: `git push origin feature/amazing-feature`
8. **Create a Pull Request**: Open a PR against the main repository

### Development Setup

```bash
# Clone the repository
git clone https://github.com/yourusername/homeassistant-api-client.git
cd homeassistant-api-client

# Build the project
mvn clean install

# Run tests
mvn test
```

### Code Style

This project follows the Google Java Style Guide. Run the following to check your style:

```bash
mvn checkstyle:check
```

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

---

Built with ❤️ for the Home Assistant community.

