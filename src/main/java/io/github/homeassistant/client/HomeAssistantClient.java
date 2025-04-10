/*
 * MIT License
 *
 * Copyright (c) 2025 Chris Collins <chris@hitorro.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.homeassistant.client;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.homeassistant.auth.AuthenticationManager;
import io.github.homeassistant.model.ApiStatus;
import io.github.homeassistant.model.Event;
import io.github.homeassistant.model.Service;
import io.github.homeassistant.model.State;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * Client for interacting with the Home Assistant REST API.
 * 
 * <p>This class provides methods to interact with various Home Assistant API endpoints
 * in a thread-safe manner. It handles authentication, request execution, and response
 * parsing, while providing proper error handling.</p>
 * 
 * <p>Usage example:</p>
 * <pre>
 * String baseUrl = "http://homeassistant.local:8123";
 * String accessToken = "your_long_lived_access_token";
 * HomeAssistantClient client = new HomeAssistantClient(baseUrl, accessToken);
 * 
 * // Get API status
 * ApiStatus status = client.getApiStatus();
 * 
 * // Get all entity states
 * List<State> states = client.getStates();
 * 
 * // Get state for a specific entity
 * State lightState = client.getState("light.living_room");
 * </pre>
 */
public class HomeAssistantClient {

    private final String baseUrl;
    private final AuthenticationManager authManager;
    private  final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Executor executor;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new Home Assistant client with the specified base URL and access token.
     *
     * @param baseUrl the base URL of the Home Assistant instance, without trailing slash
     *                (e.g., "http://homeassistant.local:8123")
     * @param accessToken the long-lived access token for authentication
     * @throws IllegalArgumentException if baseUrl or accessToken is null or empty
     */
    public HomeAssistantClient(String baseUrl, String accessToken, HttpClient client) throws AuthenticationManager.AuthenticationException {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        
        // Remove trailing slash if present
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.authManager = new AuthenticationManager(accessToken);
        if (client == null) {
            this.httpClient = HttpClients.createDefault();
        }
        else {
            this.httpClient = client;
        }

        SimpleModule module = new SimpleModule();
        module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(module);
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Gets the current status of the Home Assistant API.
     *
     * <p>This method calls the core API endpoint (/api/) to verify that the API
     * is operational and the authentication is working.</p>
     *
     * @return an ApiStatus object containing the API information
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    public ApiStatus getApiStatus() throws HomeAssistantException {
        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/").build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequest(uri, ApiStatus.class);
    }

    /**
     * Gets the states of all entities from Home Assistant.
     *
     * <p>This method retrieves the current state of all entities from the
     * /api/states endpoint.</p>
     *
     * @return a list of State objects representing all entity states
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    public List<State> getStates() throws HomeAssistantException {
        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/states").build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequestForList(uri, new TypeReference<List<State>>() {});
    }

    /**
     * Gets the state of a specific entity from Home Assistant.
     *
     * <p>This method retrieves the current state of the specified entity ID
     * from the /api/states/&lt;entity_id&gt; endpoint.</p>
     *
     * @param entityId the entity ID to retrieve the state for (e.g., "light.living_room")
     * @return a State object representing the entity state
     * @throws HomeAssistantException if there's an error communicating with the API
     * @throws IllegalArgumentException if entityId is null or empty
     */
    public State getState(String entityId) throws HomeAssistantException {
        if (entityId == null || entityId.trim().isEmpty()) {
            throw new IllegalArgumentException("Entity ID cannot be null or empty");
        }

        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/states/" + entityId).build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequest(uri, State.class);
    }

    /**
     * Gets all available event types from Home Assistant.
     *
     * <p>This method retrieves the list of available event types from the
     * /api/events endpoint.</p>
     *
     * @return a list of available event type strings
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    public List<Event> getEvents() throws HomeAssistantException {
        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/events").build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequestForList(uri, new TypeReference<List<Event>>() {});
    }

    /**
     * Asynchronously gets all available event types from Home Assistant.
     *
     * <p>This method retrieves the list of available event types from the
     * /api/events endpoint in a non-blocking way.</p>
     *
     * @return a CompletableFuture that will complete with the list of available event types
     */
    public CompletableFuture<List<Event>> getEventsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getEvents();
            } catch (HomeAssistantException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, executor);
    }

    /**
     * Fires an event with the specified type and data.
     *
     * <p>This method fires a custom event with the specified type and optional data payload
     * to the /api/events/&lt;event_type&gt; endpoint.</p>
     *
     * @param eventType the type of event to fire
     * @param eventData the data payload for the event (can be null for events without data)
     * @throws HomeAssistantException if there's an error communicating with the API
     * @throws IllegalArgumentException if eventType is null or empty
     */
    public void fireEvent(String eventType, JsonNode eventData) throws HomeAssistantException {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }

        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/events/" + eventType).build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        executePostRequest(uri, eventData == null ? objectMapper.createObjectNode() : eventData);
    }

    /**
     * Asynchronously fires an event with the specified type and data.
     *
     * <p>This method fires a custom event with the specified type and optional data payload
     * to the /api/events/&lt;event_type&gt; endpoint in a non-blocking way.</p>
     *
     * @param eventType the type of event to fire
     * @param eventData the data payload for the event (can be null for events without data)
     * @return a CompletableFuture that will complete when the event has been fired
     */
    public CompletableFuture<Void> fireEventAsync(String eventType, JsonNode eventData) throws HomeAssistantException {
        return CompletableFuture.runAsync(() -> {
            try {
                fireEvent(eventType, eventData);
            } catch (HomeAssistantException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Gets all available services from Home Assistant.
     *
     * <p>This method retrieves all available services grouped by domain from the
     * /api/services endpoint.</p>
     *
     * @return a map of domain names to maps of service names to Service objects
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    public Map<String, Map<String, Service>> getServices() throws HomeAssistantException {
        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/services").build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequest(uri, new TypeReference<Map<String, Map<String, Service>>>() {});
    }

    /**
     * Asynchronously gets all available services from Home Assistant.
     *
     * <p>This method retrieves all available services grouped by domain from the
     * /api/services endpoint in a non-blocking way.</p>
     *
     * @return a CompletableFuture that will complete with a map of domain names to maps of service names to Service objects
     */
    public CompletableFuture<Map<String, Map<String, Service>>> getServicesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getServices();
            } catch (HomeAssistantException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, executor);
    }

    /**
     * Gets all available services for a specific domain from Home Assistant.
     *
     * <p>This method retrieves all available services for the specified domain from the
     * /api/services/&lt;domain&gt; endpoint.</p>
     *
     * @param domain the domain to get services for (e.g., "light", "switch", "climate")
     * @return a map of service names to Service objects for the specified domain
     * @throws HomeAssistantException if there's an error communicating with the API
     * @throws IllegalArgumentException if domain is null or empty
     */
    public Map<String, Service> getDomainServices(String domain) throws HomeAssistantException {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be null or empty");
        }

        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/services/" + domain).build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequest(uri, new TypeReference<Map<String, Service>>() {});
    }

    /**
     * Asynchronously gets all available services for a specific domain from Home Assistant.
     *
     * <p>This method retrieves all available services for the specified domain from the
     * /api/services/&lt;domain&gt; endpoint in a non-blocking way.</p>
     *
     * @param domain the domain to get services for (e.g., "light", "switch", "climate")
     * @return a CompletableFuture that will complete with a map of service names to Service objects for the specified domain
     */
    public CompletableFuture<Map<String, Service>> getDomainServicesAsync(String domain) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getDomainServices(domain);
            } catch (HomeAssistantException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Calls a service with the specified domain, service name, and data.
     *
     * <p>This method calls a service with the specified parameters using the
     * /api/services/&lt;domain&gt;/&lt;service&gt; endpoint.</p>
     *
     * @param domain the domain of the service to call (e.g., "light", "switch")
     * @param service the name of the service to call (e.g., "turn_on", "turn_off")
     * @param serviceData the data to pass to the service (can be null for services without parameters)
     * @throws HomeAssistantException if there's an error communicating with the API
     * @throws IllegalArgumentException if domain or service is null or empty
     */
    public void callService(String domain, String service, JsonNode serviceData) throws HomeAssistantException {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be null or empty");
        }
        if (service == null || service.trim().isEmpty()) {
            throw new IllegalArgumentException("Service cannot be null or empty");
        }

        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/services/" + domain + "/" + service).build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        executePostRequest(uri, serviceData == null ? objectMapper.createObjectNode() : serviceData);
    }

    /**
     * Asynchronously calls a service with the specified domain, service name, and data.
     *
     * <p>This method calls a service with the specified parameters using the
     * /api/services/&lt;domain&gt;/&lt;service&gt; endpoint in a non-blocking way.</p>
     *
     * @param domain the domain of the service to call (e.g., "light", "switch")
     * @param service the name of the service to call (e.g., "turn_on", "turn_off")
     * @param serviceData the data to pass to the service (can be null for services without parameters)
     * @return a CompletableFuture that will complete when the service has been called
     */
    public CompletableFuture<Void> callServiceAsync(String domain, String service, JsonNode serviceData) throws HomeAssistantException {
        return CompletableFuture.runAsync(() -> {
            try {
                callService(domain, service, serviceData);
            } catch (HomeAssistantException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Executes a GET request and parses the response as a single object.
     *
     * @param uri the URI to send the request to
     * @param responseClass the class to parse the response as
     * @param <T> the type of the response object
     * @return the parsed response object
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    private <T> T executeGetRequest(URI uri, Class<T> responseClass) throws HomeAssistantException {
        lock.readLock().lock();
        try {
            HttpGet request = new HttpGet(uri);
            request.setHeader("Authorization", "Bearer " + authManager.getToken());
            request.setHeader("Content-Type", "application/json");

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (statusCode >= 200 && statusCode < 300) {
                if (entity != null) {
                    String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    try {
                        return objectMapper.readValue(json, responseClass);
                    } catch (JsonProcessingException e) {
                        throw new HomeAssistantException("Error parsing API response", e);
                    }
                } else {
                    throw new HomeAssistantException("Empty response from API");
                }
            } else {
                String errorBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                throw new HomeAssistantException(
                        "API request failed with status code " + statusCode + ": " + errorBody);
            }
        } catch (IOException e) {
            throw new HomeAssistantException("Error communicating with Home Assistant API", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Executes a GET request and parses the response as a list of objects.
     *
     * @param uri the URI to send the request to
     * @param typeReference the type reference for the list type
     * @param <T> the type of the list elements
     * @return the parsed list of objects
     * @return the parsed list of objects
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    private <T> List<T> executeGetRequestForList(URI uri, TypeReference<List<T>> typeReference)
            throws HomeAssistantException {
        lock.readLock().lock();
        try {
            HttpGet request = new HttpGet(uri);
            request.setHeader("Authorization", "Bearer " + authManager.getToken());
            request.setHeader("Content-Type", "application/json");

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (statusCode >= 200 && statusCode < 300) {
                if (entity != null) {
                    String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    try {
                        return objectMapper.readValue(json, typeReference);
                    } catch (JsonProcessingException e) {
                        throw new HomeAssistantException("Error parsing API response", e);
                    }
                } else {
                    throw new HomeAssistantException("Empty response from API");
                }
            } else {
                String errorBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                throw new HomeAssistantException(
                        "API request failed with status code " + statusCode + ": " + errorBody);
            }
        } catch (IOException e) {
            throw new HomeAssistantException("Error communicating with Home Assistant API", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Executes a GET request and parses the response as a complex object.
     *
     * @param uri the URI to send the request to
     * @param typeReference the type reference for the complex type
     * @param <T> the type of the response object
     * @return the parsed response object
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    private <T> T executeGetRequest(URI uri, TypeReference<T> typeReference) throws HomeAssistantException {
        lock.readLock().lock();
        try {
            HttpGet request = new HttpGet(uri);
            request.setHeader("Authorization", "Bearer " + authManager.getToken());
            request.setHeader("Content-Type", "application/json");

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (statusCode >= 200 && statusCode < 300) {
                if (entity != null) {
                    String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    try {
                        return objectMapper.readValue(json, typeReference);
                    } catch (JsonProcessingException e) {
                        throw new HomeAssistantException("Error parsing API response", e);
                    }
                } else {
                    throw new HomeAssistantException("Empty response from API");
                }
            } else {
                String errorBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                throw new HomeAssistantException(
                        "API request failed with status code " + statusCode + ": " + errorBody);
            }
        } catch (IOException e) {
            throw new HomeAssistantException("Error communicating with Home Assistant API", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Executes a POST request with the given URI and request body.
     *
     * @param uri the URI to send the request to
     * @param requestBody the body to include in the request
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    private void executePostRequest(URI uri, JsonNode requestBody) throws HomeAssistantException {
        lock.writeLock().lock();
        try {
            HttpPost request = new HttpPost(uri);
            request.setHeader("Authorization", "Bearer " + authManager.getToken());
            request.setHeader("Content-Type", "application/json");

            String requestJson = objectMapper.writeValueAsString(requestBody);
            request.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (!(statusCode >= 200 && statusCode < 300)) {
                String errorBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                throw new HomeAssistantException(
                        "API request failed with status code " + statusCode + ": " + errorBody);
            }

            if (entity != null) {
                EntityUtils.consume(entity);
            }
        } catch (IOException e) {
            throw new HomeAssistantException("Error communicating with Home Assistant API", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Executes a POST request with the given URI and request body and parses the response.
     *
     * @param uri the URI to send the request to
     * @param requestBody the body to include in the request
     * @param responseClass the class to parse the response as
     * @param <T> the type of the response object
     * @return the parsed response object
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    private <T> T executePostRequestWithResponse(URI uri, JsonNode requestBody, Class<T> responseClass) throws HomeAssistantException {
        lock.writeLock().lock();
        try {
            HttpPost request = new HttpPost(uri);
            request.setHeader("Authorization", "Bearer " + authManager.getToken());
            request.setHeader("Content-Type", "application/json");

            String requestJson = objectMapper.writeValueAsString(requestBody);
            request.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (statusCode >= 200 && statusCode < 300) {
                if (entity != null) {
                    String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    try {
                        return objectMapper.readValue(json, responseClass);
                    } catch (JsonProcessingException e) {
                        throw new HomeAssistantException("Error parsing API response", e);
                    }
                } else {
                    throw new HomeAssistantException("Empty response from API");
                }
            } else {
                String errorBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                throw new HomeAssistantException(
                        "API request failed with status code " + statusCode + ": " + errorBody);
            }
        } catch (IOException e) {
            throw new HomeAssistantException("Error communicating with Home Assistant API", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Executes a POST request with the given URI and request body and parses the response as a complex type.
     *
     * @param uri the URI to send the request to
     * @param requestBody the body to include in the request
     * @param typeReference the type reference for the complex type
     * @param <T> the type of the response object
     * @return the parsed response object
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    private <T> T executePostRequestWithResponse(URI uri, JsonNode requestBody, TypeReference<T> typeReference) throws HomeAssistantException {
        lock.writeLock().lock();
        try {
            HttpPost request = new HttpPost(uri);
            request.setHeader("Authorization", "Bearer " + authManager.getToken());
            request.setHeader("Content-Type", "application/json");

            String requestJson = objectMapper.writeValueAsString(requestBody);
            request.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (statusCode >= 200 && statusCode < 300) {
                if (entity != null) {
                    String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    try {
                        return objectMapper.readValue(json, typeReference);
                    } catch (JsonProcessingException e) {
                        throw new HomeAssistantException("Error parsing API response", e);
                    }
                } else {
                    throw new HomeAssistantException("Empty response from API");
                }
            } else {
                String errorBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                throw new HomeAssistantException(
                        "API request failed with status code " + statusCode + ": " + errorBody);
            }
        } catch (IOException e) {
            throw new HomeAssistantException("Error communicating with Home Assistant API", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the base URL of the Home Assistant instance.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomeAssistantClient that = (HomeAssistantClient) o;
        return Objects.equals(baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl);
    }

    @Override
    public String toString() {
        return "HomeAssistantClient{" +
                "baseUrl='" + baseUrl + '\'' +
                '}';
    }
    
    /**
     * Gets the current configuration information from Home Assistant.
     *
     * <p>This method retrieves the current system configuration from the
     * /api/config endpoint.</p>
     *
     * @return a map containing configuration details
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    public Map<String, Object> getConfig() throws HomeAssistantException {
        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/config").build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequest(uri, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Gets the error log from Home Assistant.
     *
     * <p>This method retrieves the current error log from the
     * /api/error_log endpoint.</p>
     *
     * @return a list of maps containing error log entries
     * @throws HomeAssistantException if there's an error communicating with the API
     */
    public List<Map<String, Object>> getErrorLog() throws HomeAssistantException {
        URI uri;
        try {
            uri = new URIBuilder(baseUrl + "/api/error_log").build();
        } catch (URISyntaxException e) {
            throw new HomeAssistantException("Invalid API URI", e);
        }

        return executeGetRequest(uri, new TypeReference<List<Map<String, Object>>>() {});
    }
}

