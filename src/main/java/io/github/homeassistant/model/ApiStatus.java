package io.github.homeassistant.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents the status response from the Home Assistant API's root endpoint (/api/).
 * 
 * <p>This class contains information about the API such as the current version
 * and message returned by the server.</p>
 */
public class ApiStatus {

    @JsonProperty("message")
    private String message;

    @JsonProperty("version")
    private String version;

    /**
     * Default constructor for deserialization.
     */
    public ApiStatus() {
    }

    /**
     * Creates a new API status object with the specified message and version.
     *
     * @param message the status message
     * @param version the API version
     */
    public ApiStatus(String message, String version) {
        this.message = message;
        this.version = version;
    }

    /**
     * Gets the status message.
     *
     * @return the status message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the status message.
     *
     * @param message the status message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the API version.
     *
     * @return the API version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the API version.
     *
     * @param version the API version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiStatus apiStatus = (ApiStatus) o;
        return Objects.equals(message, apiStatus.message) &&
                Objects.equals(version, apiStatus.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, version);
    }

    @Override
    public String toString() {
        return "ApiStatus{" +
                "message='" + message + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

