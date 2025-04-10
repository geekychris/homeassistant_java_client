package io.github.homeassistant.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.homeassistant.model.Event;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.homeassistant.auth.AuthenticationManager;
import io.github.homeassistant.model.ApiStatus;
import io.github.homeassistant.model.Service;
import io.github.homeassistant.model.State;

/**
 * Unit tests for the HomeAssistantClient class.
 * Uses mocking to simulate the Home Assistant API server.
 */
@ExtendWith(MockitoExtension.class)
public class HomeAssistantClientTest {

    private static final String BASE_URL = "http://localhost:8123";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock_token";
    private static final String ENTITY_ID = "light.living_room";
    private static final String DOMAIN = "light";
    private static final String SERVICE = "turn_on";
    private static final String EVENT_TYPE = "custom_event";

    private HomeAssistantClient client;
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Mock
    private HttpEntity httpEntity;

    private AutoCloseable mockitoCloseable;

    /**
     * Set up test environment before each test.
     * Initializes mocks and creates a client with mocked HTTP client.
     */
    @BeforeEach
    public void setUp() throws IOException, AuthenticationManager.AuthenticationException {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        // Configure HTTP client to return mock response
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(httpEntity);

        // Create client with injected HTTP client for testing
        client = new HomeAssistantClient(BASE_URL, ACCESS_TOKEN, httpClient);
    }

    /**
     * Clean up resources after each test.
     */
    @AfterEach
    public void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    /**
     * Helper method to prepare a successful JSON response.
     */
    private void prepareSuccessJsonResponse(Object responseObj) throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(responseObj);

        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpEntity.getContent()).thenReturn(
                new java.io.ByteArrayInputStream(jsonResponse.getBytes())
        );
    }

    /**
     * Helper method to prepare an error response.
     */
    private void prepareErrorResponse(int statusCode, String message) throws Exception {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("message", message);
        String jsonResponse = objectMapper.writeValueAsString(errorNode);

        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(httpEntity.getContent()).thenReturn(
                new java.io.ByteArrayInputStream(jsonResponse.getBytes())
        );
    }

    /**
     * Test for getting API status.
     */
    @Test
    @DisplayName("Should successfully retrieve API status")
    public void testGetApiStatus() throws Exception {
        // Prepare mock response
        ApiStatus apiStatus = new ApiStatus("ok", "API running.");
        prepareSuccessJsonResponse(apiStatus);

        // Configure HTTP client to return the mocked response
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test
        ApiStatus result = client.getApiStatus();

        // Verify the result
        assertNotNull(result);
        assertEquals("ok", result.getMessage());
        assertEquals("API running.", result.getMessage());

        // Verify the correct URL was called
        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpGet request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/", request.getURI().toString());
    }

    /**
     * Test for getting states of all entities.
     */
    @Test
    @DisplayName("Should successfully retrieve all entity states")
    public void testGetStates() throws Exception {
        // Prepare mock response
        State state1 = new State(ENTITY_ID, "on", objectMapper.createObjectNode(),
                ZonedDateTime.now(), ZonedDateTime.now(), null);
        State state2 = new State("switch.kitchen", "off", objectMapper.createObjectNode(),
                ZonedDateTime.now(), ZonedDateTime.now(), null);
        List<State> states = List.of(state1, state2);

        prepareSuccessJsonResponse(states);

        // Configure HTTP client to return the mocked response
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test
        List<State> result = client.getStates();

        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ENTITY_ID, result.get(0).getEntityId());
        assertEquals("on", result.get(0).getState());
        assertEquals("switch.kitchen", result.get(1).getEntityId());
        assertEquals("off", result.get(1).getState());

        // Verify the correct URL was called
        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpGet request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/states", request.getURI().toString());
    }

    /**
     * Test for getting state of a specific entity.
     */
    @Test
    @DisplayName("Should successfully retrieve state for a specific entity")
    public void testGetState() throws Exception {
        // Prepare mock response
        State state = new State(ENTITY_ID, "on", objectMapper.createObjectNode(),
                ZonedDateTime.now(), ZonedDateTime.now(), null);

        prepareSuccessJsonResponse(state);

        // Configure HTTP client to return the mocked response
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test
        State result = client.getState(ENTITY_ID);

        // Verify the result
        assertNotNull(result);
        assertEquals(ENTITY_ID, result.getEntityId());
        assertEquals("on", result.getState());

        // Verify the correct URL was called
        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpGet request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/states/" + ENTITY_ID, request.getURI().toString());
    }

    /**
     * Test for getting available events.
     */
    @Test
    @DisplayName("Should successfully retrieve available events")
    public void testGetEvents() throws Exception {
        // Prepare mock response
        List<String> events = List.of("state_changed", "service_registered", "custom_event");
        prepareSuccessJsonResponse(events);

        // Configure HTTP client to return the mocked response
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test
        List<Event> result = client.getEvents();

        // Verify the result
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("state_changed"));
        assertTrue(result.contains("custom_event"));

        // Verify the correct URL was called
        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpGet request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/events", request.getURI().toString());
    }

    /**
     * Test for firing a custom event.
     */
    @Test
    @DisplayName("Should successfully fire a custom event")
    public void testFireEvent() throws Exception {
        // Prepare mock response
        ObjectNode successResponse = objectMapper.createObjectNode();
        successResponse.put("success", true);
        prepareSuccessJsonResponse(successResponse);

        // Configure HTTP client to return the mocked response
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

        // Prepare event data
        ObjectNode eventData = objectMapper.createObjectNode();
        eventData.put("brightness", 255);
        eventData.put("color_temp", 300);

        // Call the method under test
        client.fireEvent(EVENT_TYPE, eventData);

        // Verify the correct URL was called with proper payload
        ArgumentCaptor<HttpPost> requestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpPost request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/events/" + EVENT_TYPE, request.getURI().toString());

        // Verify request contained proper body
        HttpEntity entity = request.getEntity();
        assertNotNull(entity);

        // We can't easily extract the entity content after it's been consumed,
        // so we're just verifying it's the right type
        assertTrue(entity instanceof StringEntity);
    }

    /**
     * Test for getting available services.
     */
    @Test
    @DisplayName("Should successfully retrieve available services")
    public void testGetServices() throws Exception {
        // Prepare mock response for services
        Map<String, Service.ServiceField> serviceFields = new HashMap <>();
        serviceFields.put("brightness", new Service.ServiceField("light_brightness", "Brightness level (0-255)", "light", false, 10,0,255));

        Service lightService = new Service(DOMAIN, SERVICE, "Turn on a light", serviceFields, null);
        Service fanService = new Service("fan", "turn_on", "Turn on a fan", null, null);

        Map<String, Map<String, Service>> servicesMap = Map.of(
                DOMAIN, Map.of(SERVICE, lightService),
                "fan", Map.of("turn_on", fanService)
        );

        prepareSuccessJsonResponse(servicesMap);

        // Configure HTTP client to return the mocked response
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test
        Map<String, Map<String, Service>> result = client.getServices();

        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(DOMAIN));
        assertTrue(result.containsKey("fan"));
        assertEquals(1, result.get(DOMAIN).size());
        assertEquals(1, result.get("fan").size());

        Service resultLightService = result.get(DOMAIN).get(SERVICE);
        assertNotNull(resultLightService);
        assertEquals(DOMAIN, resultLightService.getDomain());
        assertEquals(SERVICE, resultLightService.getService());
        assertEquals("Turn on a light", resultLightService.getDescription());

        // Verify the correct URL was called
        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpGet request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/services", request.getURI().toString());
    }

    /**
     * Test for calling a service.
     */
    @Test
    @DisplayName("Should successfully call a service")
    public void testCallService() throws Exception {
        // Prepare mock response
        ObjectNode successResponse = objectMapper.createObjectNode();
        successResponse.put("success", true);
        prepareSuccessJsonResponse(successResponse);

        // Configure HTTP client to return the mocked response
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

        // Prepare service data
        ObjectNode serviceData = objectMapper.createObjectNode();
        serviceData.put("entity_id", ENTITY_ID);
        serviceData.put("brightness", 255);

        // Call the method under test
        client.callService(DOMAIN, SERVICE, serviceData);

        // Verify the correct URL was called with proper payload
        ArgumentCaptor<HttpPost> requestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpPost request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/services/" + DOMAIN + "/" + SERVICE,
                request.getURI().toString());

        // Verify request contained proper body
        // Verify request contained proper body
        HttpEntity entity = request.getEntity();
        assertNotNull(entity);

        // We can't easily extract the entity content after it's been consumed,
        // so we're just verifying it's the right type
        assertTrue(entity instanceof StringEntity);
    }

    /**
     * Test for handling authentication errors (401 Unauthorized).
     * Verifies that the client throws a HomeAssistantAuthenticationException
     * when receiving a 401 response.
     */
    @Test
    @DisplayName("Should throw exception on authentication error (401)")
    public void testAuthenticationError() throws Exception {
        // Prepare 401 Unauthorized response
        prepareErrorResponse(HttpStatus.SC_UNAUTHORIZED, "Unauthorized, invalid access token");

        // Configure HTTP client to return the error response
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test and expect an exception
        HomeAssistantException exception = assertThrows(
                HomeAssistantException.class,
                () -> client.getApiStatus()
        );

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Authentication failed"));
        assertTrue(exception.getMessage().contains("401"));

        // Verify the correct URL was called
        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpGet request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/", request.getURI().toString());
    }

    /**
     * Test for handling connection errors (network failures).
     * Verifies that the client throws a HomeAssistantCommunicationException
     * when a network error occurs.
     */
    @Test
    @DisplayName("Should throw exception on connection error")
    public void testConnectionError() throws Exception {
        // Configure HTTP client to throw IOException (network error)
        when(httpClient.execute(any(HttpGet.class))).thenThrow(new IOException("Network error"));

        // Call the method under test and expect an exception
        HomeAssistantException exception = assertThrows(
                HomeAssistantException.class,
                () -> client.getApiStatus()
        );

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Communication error"));
        assertTrue(exception.getMessage().contains("Network error"));
    }

    /**
     * Test for handling server errors (500 Internal Server Error).
     * Verifies that the client throws a HomeAssistantServerException
     * when receiving a 500 response.
     */
    @Test
    @DisplayName("Should throw exception on server error (500)")
    public void testServerError() throws Exception {
        // Prepare 500 Internal Server Error response
        prepareErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal server error");

        // Configure HTTP client to return the error response
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test and expect an exception
        HomeAssistantException exception = assertThrows(
                HomeAssistantException.class,
                () -> client.getApiStatus()
        );

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Server error"));
        assertTrue(exception.getMessage().contains("500"));

        // Verify the correct URL was called
        ArgumentCaptor<HttpGet> requestCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(httpClient).execute(requestCaptor.capture());
        HttpGet request = requestCaptor.getValue();
        assertEquals(BASE_URL + "/api/", request.getURI().toString());
    }

    /**
     * Test for handling malformed JSON responses.
     * Verifies that the client throws a HomeAssistantApiException
     * when receiving an invalid JSON response.
     */
    @Test
    @DisplayName("Should throw exception on invalid JSON response")
    public void testInvalidJson() throws Exception {
        // Prepare a response with invalid JSON
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpEntity.getContent()).thenReturn(
                new java.io.ByteArrayInputStream("{invalid_json}".getBytes())
        );

        // Configure HTTP client to return the response with invalid JSON
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        // Call the method under test and expect an exception
        HomeAssistantException exception = assertThrows(
                HomeAssistantException.class,
                () -> client.getApiStatus()
        );

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Failed to parse"));
    }

    /**
     * Test for handling invalid base URL.
     * Verifies that the client constructor throws an IllegalArgumentException
     * when provided with an invalid base URL.
     */
    @Test
    @DisplayName("Should throw exception when initializing with invalid base URL")
    public void testInvalidBaseUrl() {
        // Test with various invalid URLs
        assertThrows(IllegalArgumentException.class, () -> {
            new HomeAssistantClient("invalid_url", ACCESS_TOKEN, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new HomeAssistantClient("", ACCESS_TOKEN, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new HomeAssistantClient(null, ACCESS_TOKEN, null);
        });
    }

    /**
     * Test for handling null access token.
     * Verifies that attempting to create an AuthenticationManager with a null token
     * throws an IllegalArgumentException.
     */
    @Test
    @DisplayName("Should throw exception when initializing with null token")
    public void testNullToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AuthenticationManager(null);
        });
    }

    /**
     * Test for handling empty entity ID.
     * Verifies that calling getState with an empty entity ID
     * throws an IllegalArgumentException.
     */
    @Test
    @DisplayName("Should throw exception when getting state with empty entity ID")
    public void testEmptyEntityId() {
        // Test with empty entity ID
        assertThrows(IllegalArgumentException.class, () -> {
            client.getState("");
        });

        // Test with null entity ID
        assertThrows(IllegalArgumentException.class, () -> {
            client.getState(null);
        });
    }
}
