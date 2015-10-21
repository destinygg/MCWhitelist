/**
 *
 */
package org.destinygg.mcwhitelist.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.destinygg.mcwhitelist.auth.AuthResponse.AuthResponseType;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Authentication REST client to interacy with the destiny.gg API
 *
 * @author xtphty
 */
public class DestinyGGAuthServiceImpl implements AuthService {
    private static Logger LOGGER = Logger.getLogger(DestinyGGAuthServiceImpl.class.getName());

    private String privateKey;
    private String baseUrl;
    private String authUrl;
    private Map<String, CachedAuthUser> authCache;

    public DestinyGGAuthServiceImpl(String privateKey, String authUrl) {
        this.privateKey = privateKey;
        this.baseUrl = authUrl;
        this.authUrl = this.baseUrl;
        this.authCache = Collections.synchronizedMap(new HashMap<String, CachedAuthUser>());
    }

    /**
     * Try and update a valid cached user object if needed
     *
     * @param cachedUser the cache item to test
     * @param ipAddress  the user's ip address
     * @throws IOException
     */
    public void refreshUser(CachedAuthUser cachedUser, String ipAddress) throws IOException {
        if (!cachedUser.getIpAddress().equals(ipAddress)) {
            LOGGER.log(Level.WARNING,
                    "New ip address, ignoring cache: " + cachedUser.getMCName() + " -> " + ipAddress);
            cachedUser.invalidateAuth();
            return;
        }

        if (!cachedUser.isAuthExpired()) {
            // Cached item is still valid, skip authentication
            return;
        }

        // Perform a refresh request and return
        JSONObject responseData = null;
        try {
            HttpResponse<String> httpResponse = Unirest.get(this.authUrl).header("accept", "application/json")
                    .queryString("privatekey", privateKey).queryString("uuid", cachedUser.getMCUUID())
                    .queryString("ipaddress", ipAddress).asString();

            if (isValidResponseStatus(httpResponse.getStatus())) {
                responseData = new JsonNode(httpResponse.getBody()).getObject();
                cachedUser.setSubscriptionEndTimestamp(responseData.getLong("end"));
                cachedUser.resetCacheTimestamp();
                LOGGER.log(Level.INFO,
                        "Refresh completed: " + cachedUser.getMCName() + " -> " + cachedUser.getLastRefreshTimestamp());
            } else {
                LOGGER.log(Level.WARNING,
                        "Refresh rejected: " + cachedUser.getMCName() + " -> " + httpResponse.getStatusText());
                cachedUser.invalidateAuth();
            }
        } catch (UnirestException e) {
            LOGGER.log(Level.WARNING, "Refresh failed for " + cachedUser.getMCName() + " due to: " + e.getMessage());
            cachedUser.invalidateAuth();
            e.printStackTrace();
        }
    }

    @Override
    public AuthResponse authenticateUser(String mcName, String mcUUID, String ipAddress) throws IOException {
        CachedAuthUser authUser = authCache.get(mcUUID);

        if (authUser != null && authUser.isValid()) {
            // Already know this user, try and refresh if needed
            refreshUser(authUser, ipAddress);
        }

        // Check if a new authentication is required
        if (authUser != null && authUser.isValid() && !authUser.isAuthExpired()) {
            // Cached user is valid, approve auth
            LOGGER.log(Level.INFO,
                    "Validated user " + mcName + " from cache, TTL (hours) " + authUser.getCacheTTL());
            return new AuthResponse(authUser, AuthResponseType.VALID_AUTH);
        } else {
            // New authentication is required, null ref cached auth if any
            authUser = null;
            JSONObject responseData = null;
            try {
                HttpResponse<String> httpResponse = Unirest.post(authUrl).
                        header("accept", "application/json").
                        field("privatekey", privateKey).
                        field("uuid", mcUUID).
                        field("name", mcName).
                        field("ipaddress", ipAddress).
                        asString();

                if (!isValidResponseStatus(httpResponse.getStatus())) {
                    String body = httpResponse.getBody();
                    if (body == null) {
                        body = "No reason provided";
                    }
                    LOGGER.log(Level.WARNING,
                            "Authentication rejected, with code " + httpResponse.getStatusText() + ", reason: " + body);

                    // Parse failure message and create error response object.
                    AuthResponse failureResponse = new AuthResponse(null, AuthResponseType.BAD_REQUEST);
                    if (httpResponse.getStatus() == 404) {
                        failureResponse.authResponseType = AuthResponseType.USER_NOT_FOUND;
                    } else if (httpResponse.getStatus() == 403) {
                        if (StringUtils.containsIgnoreCase(httpResponse.getBody(), "userBanned")) {
                            failureResponse.authResponseType = AuthResponseType.USER_BANNED;
                        } else if (StringUtils.containsIgnoreCase(httpResponse.getBody(), "uuidAlreadySet")) {
                            failureResponse.authResponseType = AuthResponseType.UUID_ALREADY_TAKEN;
                        } else {
                            failureResponse.authResponseType = AuthResponseType.USER_NOT_SUB;
                        }
                    } else if (httpResponse.getStatus() == 500) {
                        failureResponse.authResponseType = AuthResponseType.UUID_ALREADY_TAKEN;
                    }

                    // Return immediately as failure are not cached
                    return failureResponse;
                }

                // Serialize json response data and parse to AuthUser instance
                responseData = new JsonNode(httpResponse.getBody()).getObject();
                authUser = new DestinyGGUserImpl(responseData.getString("nick"), ipAddress, responseData.getLong("end"));
                authUser.setMCName(mcName);
                authUser.setMCUUID(mcUUID);

            } catch (UnirestException e) {
                LOGGER.log(Level.WARNING, "Authentication failed for " + mcName + ", with error: " + e.getMessage());
                e.printStackTrace();
                return new AuthResponse(authUser, AuthResponseType.BAD_RESPONSE);
            }
        }

        if (authUser.isValid()) {
            // Valid destiny.gg user linked, check subscription and cache
            authCache.put(mcUUID, authUser);
            if (authUser.isAuthExpired()) {
                return new AuthResponse(authUser, AuthResponseType.USER_NOT_SUB);
            } else {
                return new AuthResponse(authUser, AuthResponseType.VALID_AUTH);
            }
        } else {
            // Invalid response / user, remove from cache and set null
            authCache.put(mcUUID, null);
            authUser = null;
            return new AuthResponse(authUser, AuthResponseType.BAD_RESPONSE);
        }
    }

    private boolean isValidResponseStatus(int status) {
        return status == 200 || status == 201 || status == 202 || status == 204;
    }

    @Override
    public String getPlayerLoginId(String mcUUID) {
        AuthUser user = authCache.get(mcUUID);
        if (user != null && user.isValid()) {
            return user.getLoginId();
        } else {
            return null;
        }
    }
}
