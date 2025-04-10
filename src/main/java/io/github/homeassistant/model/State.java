package io.github.homeassistant.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a Home Assistant entity state.
 * 
 * This class models the state of an entity in Home Assistant, including its current state value,
 * attributes, timestamps for when it was last changed or updated, and context information.
 * 
 * @since 1.0.0
 */

/*

{"entity_id":"binary_sensor.tz3000_gntwytxo_ts0203_opening","state":"off","attributes":{"device_class":"opening","friendly_name":"Refrigerator Door"},"last_changed":"2025-03-25T04:50:56.076866+00:00","last_reported":"2025-03-25T04:50:56.076866+00:00","last_updated":"2025-03-25T04:50:56.076866+00:00","context":{"id":"01JQ5T7AYC7V1XG9VT1ASQS3M5","parent_id":null,"user_id":null}}
 */
public class State {

    @JsonProperty("entity_id")
    private String entityId;

    @JsonProperty("state")
    private String state;

    @JsonProperty("attributes")
    private JsonNode attributes;

    @JsonProperty("last_changed")
    //2025-03-25T04:50:56.076866+00:00

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSxxx")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime lastChanged;

    @JsonProperty("last_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSxxx")
    private ZonedDateTime lastUpdated;

    @JsonProperty("last_reported")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSxxx")
    private ZonedDateTime lastReported;
    @JsonProperty("context")
    private Map<String, Object> context;

    /**
     * Default constructor for the State class.
     * 
     * Required for Jackson deserialization.
     */
    public State() {
        // Default constructor for Jackson
    }

    /**
     * Constructs a new State object with the specified parameters.
     *
     * @param entityId The unique identifier for the entity
     * @param state The current state value
     * @param attributes Additional attributes of the entity
     * @param lastChanged The timestamp when the state was last changed
     * @param lastUpdated The timestamp when the state was last updated
     * @param context Additional context information
     */
    public State(String entityId, String state, JsonNode attributes, 
                ZonedDateTime lastChanged, ZonedDateTime lastUpdated, 
                Map<String, Object> context) {
        this.entityId = entityId;
        this.state = state;
        this.attributes = attributes;
        this.lastChanged = lastChanged;
        this.lastUpdated = lastUpdated;
        this.context = context;
    }

    /**
     * Gets the entity ID.
     *
     * @return The entity ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Sets the entity ID.
     *
     * @param entityId The entity ID to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Gets the state value.
     *
     * @return The current state value
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state value.
     *
     * @param state The state value to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the attributes.
     *
     * @return The attributes as a JsonNode
     */
    public JsonNode getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes The attributes to set
     */
    public void setAttributes(JsonNode attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets the timestamp when the state was last changed.
     *
     * @return The last changed timestamp
     */
    public ZonedDateTime getLastChanged() {
        return lastChanged;
    }

    /**
     * Sets the timestamp when the state was last changed.
     *
     * @param lastChanged The last changed timestamp to set
     */
    public void setLastChanged(ZonedDateTime lastChanged) {
        this.lastChanged = lastChanged;
    }

    /**
     * Gets the timestamp when the state was last updated.
     *
     * @return The last updated timestamp
     */
    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets the timestamp when the state was last updated.
     *
     * @param lastUpdated The last updated timestamp to set
     */
    public void setLastUpdated(ZonedDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Gets the context information.
     *
     * @return The context as a Map
     */
    public Map<String, Object> getContext() {
        return context;
    }

    /**
     * Sets the context information.
     *
     * @param context The context to set
     */
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state1 = (State) o;
        return Objects.equals(entityId, state1.entityId) &&
               Objects.equals(state, state1.state) &&
               Objects.equals(attributes, state1.attributes) &&
               Objects.equals(lastChanged, state1.lastChanged) &&
               Objects.equals(lastUpdated, state1.lastUpdated) &&
               Objects.equals(context, state1.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, state, attributes, lastChanged, lastUpdated, context);
    }

    @Override
    public String toString() {
        return "State{" +
               "entityId='" + entityId + '\'' +
               ", state='" + state + '\'' +
               ", attributes=" + attributes +
               ", lastChanged=" + lastChanged +
               ", lastUpdated=" + lastUpdated +
               ", context=" + context +
               '}';
    }
}
