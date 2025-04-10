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

