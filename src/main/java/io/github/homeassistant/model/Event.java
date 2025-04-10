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

package io.github.homeassistant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an event from Home Assistant.
 * 
 * <p>This class models both event types (available events that can be fired) and 
 * event instances (actual events that have occurred) in Home Assistant.</p>
 * 
 * <p>Events in Home Assistant are the foundation of automations and represent state changes,
 * service calls, and other occurrences within the system. Events have a type, data payload,
 * origin information, a timestamp, and contextual information.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Creating an event
 * Map<String, Object> context = new HashMap<>();
 * context.put("id", "abc123");
 * context.put("parent_id", null);
 * 
 * Event event = new Event("state_changed", 
 *                         jsonNodeData, 
 *                         "LOCAL", 
 *                         ZonedDateTime.now(),
 *                         context);
 * </pre>
 * 
 * @see <a href="https://developers.home-assistant.io/docs/api/websocket/#subscribe-to-events">
 *      Home Assistant Event Documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {

    /**
     * The type of event (e.g., "state_changed", "call_service").
     */
    private String eventType;
    
    /**
     * The event data payload, which varies depending on the event type.
     * Using JsonNode for flexibility as different events have different data structures.
     */
    private JsonNode eventData;
    
    /**
     * The origin of the event (e.g., "LOCAL", "REMOTE").
     */
    private String origin;
    
    /**
     * The timestamp when the event was fired.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timeFired;
    
    /**
     * Contextual information about the event, which may include IDs to relate
     * events to each other.
     */
    private Map<String, Object> context;

    /**
     * Default constructor for Jackson deserialization.
     */
    public Event() {
        this.context = new HashMap<>();
    }

    /**
     * Constructs an Event with the specified parameters.
     *
     * @param eventType The type of the event
     * @param eventData The data payload of the event
     * @param origin The origin of the event
     * @param timeFired The time the event was fired
     * @param context Contextual information about the event
     */
    @JsonCreator
    public Event(
            @JsonProperty("event_type") String eventType,
            @JsonProperty("event_data") JsonNode eventData,
            @JsonProperty("origin") String origin,
            @JsonProperty("time_fired") ZonedDateTime timeFired,
            @JsonProperty("context") Map<String, Object> context) {
        this.eventType = eventType;
        this.eventData = eventData;
        this.origin = origin;
        this.timeFired = timeFired;
        this.context = context != null ? context : new HashMap<>();
    }

    /**
     * Gets the event type.
     *
     * @return The event type
     */
    @JsonProperty("event_type")
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the event type.
     *
     * @param eventType The event type to set
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Gets the event data payload.
     *
     * @return The event data as a JsonNode
     */
    @JsonProperty("event_data")
    public JsonNode getEventData() {
        return eventData;
    }

    /**
     * Sets the event data payload.
     *
     * @param eventData The event data to set
     */
    public void setEventData(JsonNode eventData) {
        this.eventData = eventData;
    }

    /**
     * Gets the origin of the event.
     *
     * @return The origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the origin of the event.
     *
     * @param origin The origin to set
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * Gets the time the event was fired.
     *
     * @return The time fired as a ZonedDateTime
     */
    @JsonProperty("time_fired")
    public ZonedDateTime getTimeFired() {
        return timeFired;
    }

    /**
     * Sets the time the event was fired.
     *
     * @param timeFired The time fired to set
     */
    public void setTimeFired(ZonedDateTime timeFired) {
        this.timeFired = timeFired;
    }

    /**
     * Gets the context information for the event.
     *
     * @return The context as a Map
     */
    public Map<String, Object> getContext() {
        return context;
    }

    /**
     * Sets the context information for the event.
     *
     * @param context The context to set
     */
    public void setContext(Map<String, Object> context) {
        this.context = context != null ? context : new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(eventType, event.eventType) &&
               Objects.equals(eventData, event.eventData) &&
               Objects.equals(origin, event.origin) &&
               Objects.equals(timeFired, event.timeFired) &&
               Objects.equals(context, event.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, eventData, origin, timeFired, context);
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventType='" + eventType + '\'' +
                ", eventData=" + eventData +
                ", origin='" + origin + '\'' +
                ", timeFired=" + timeFired +
                ", context=" + context +
                '}';
    }
}

