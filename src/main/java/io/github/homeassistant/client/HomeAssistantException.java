package io.github.homeassistant.client;

/**
 * Exception thrown for errors that occur when interacting with the Home Assistant API.
 * 
 * <p>This exception encapsulates various errors such as:</p>
 * <ul>
 *   <li>Network connectivity issues</li>
 *   <li>Authentication failures</li>
 *   <li>API response parsing errors</li>
 *   <li>Invalid request parameters</li>
 *   <li>Server-side errors reported by the API</li>
 * </ul>
 */
public class HomeAssistantException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public HomeAssistantException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public HomeAssistantException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public HomeAssistantException(Throwable cause) {
        super(cause);
    }
}

