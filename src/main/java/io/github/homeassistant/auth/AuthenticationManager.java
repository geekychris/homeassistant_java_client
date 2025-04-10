package io.github.homeassistant.auth;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;

/**
 * Manages authentication for Home Assistant API requests.
 *
 * <p>This class handles the storage, validation, and usage of long-lived access tokens
 * for authenticating with the Home Assistant API. It provides thread-safe access to token
 * operations and methods to create authenticated HTTP headers for API requests.
 *
 * <p>Example usage:
 * <pre>
 * try {
 *   AuthenticationManager authManager = new AuthenticationManager("your-access-token");
 *   Header authHeader = authManager.createAuthHeader();
 *   // Use authHeader in HTTP requests
 * } catch (AuthenticationException e) {
 *   // Handle authentication errors
 * }
 * </pre>
 */
public class AuthenticationManager {
  /** Pattern for validating Home Assistant access tokens. */
  private static final Pattern TOKEN_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");
  
  /** Minimum valid token length. */
  private static final int MIN_TOKEN_LENGTH = 32;
  
  /** Lock for thread-safe access to the token. */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  
  /** Access token for Home Assistant API authentication. */
  private String accessToken;
  
  /** Access token alias for compatibility. */
  private String token;

  /**
   * Creates a new authentication manager with the specified access token.
   *
   * @param accessToken the long-lived access token for Home Assistant API
   * @throws AuthenticationException if the token is invalid or null
   */
  public AuthenticationManager(String accessToken) throws AuthenticationException {
    updateToken(accessToken);
  }

  /**
   * Returns the current access token.
   *
   * @return the current access token
   */
  public String getAccessToken() {
    lock.readLock().lock();
    try {
      return accessToken;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Updates the access token with a new value.
   *
   * @param newToken the new access token to use
   * @throws AuthenticationException if the new token is invalid or null
   */
  public void updateToken(String newToken) throws AuthenticationException {
    validateToken(newToken);
    
    lock.writeLock().lock();
    try {
      this.accessToken = newToken;
      this.token = newToken;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Creates an HTTP Authorization header with the access token.
   *
   * @return an HTTP header containing the authorization token
   */
  public Header createAuthHeader() {
    lock.readLock().lock();
    try {
      return new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Gets the current authentication token.
   *
   * @return The current authentication token
   */
  public String getToken() {
      return this.token;
  }
  /**
   * Validates that the token meets all requirements for a Home Assistant access token.
   *
   * @param token the token to validate
   * @throws AuthenticationException if the token is invalid
   */
  private void validateToken(String token) throws AuthenticationException {
    if (token == null) {
      throw new AuthenticationException("Access token cannot be null");
    }
    
    if (token.trim().isEmpty()) {
      throw new AuthenticationException("Access token cannot be empty");
    }
    
    if (token.length() < MIN_TOKEN_LENGTH) {
      throw new AuthenticationException(
          String.format("Access token must be at least %d characters", MIN_TOKEN_LENGTH));
    }
    
    if (!TOKEN_PATTERN.matcher(token).matches()) {
      throw new AuthenticationException(
          "Access token contains invalid characters. Only alphanumeric characters, dots, "
          + "underscores, and hyphens are allowed.");
    }
  }

  /**
   * Exception thrown when there is an issue with Home Assistant authentication.
   */
  public static class AuthenticationException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new authentication exception with the specified message.
     *
     * @param message the detail message
     */
    public AuthenticationException(String message) {
      super(message);
    }

    /**
     * Creates a new authentication exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public AuthenticationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}

