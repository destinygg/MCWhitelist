/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 *
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
	 * @param cachedUser
	 *            the cache item to test
	 * @throws IOException
	 */
	public void refreshUser(CachedAuthUser cachedUser) throws IOException {
		if (!cachedUser.isCacheExpired()) {
			return;
		}

		JSONObject responseData = null;
		try {
			HttpResponse<String> httpResponse = Unirest.get(this.authUrl).header("accept", "application/json")
					.queryString("privatekey", privateKey).queryString("uuid", cachedUser.getMCUUID()).asString();

			if (!isValidResponseStatus(httpResponse.getStatus())) {
				LOGGER.log(Level.WARNING,
						"Refresh rejected: " + cachedUser.getMCName() + " -> " + httpResponse.getStatusText());
				return;
			}
			responseData = new JsonNode(httpResponse.getBody()).getObject();

		} catch (UnirestException e) {
			LOGGER.log(Level.WARNING, "Refresh failed: " + cachedUser.getMCName());
			e.printStackTrace();
			return;
		}

		cachedUser.setSubscriptionEndTimestamp(responseData.getLong("end"));
		cachedUser.resetCacheTimestamp();
		LOGGER.log(Level.INFO, "Refresh completed: " + cachedUser.getMCName() + ", " + cachedUser.getLastRefreshTimestamp());
	}

	@Override
	public AuthResponse authenticateUser(String mcName, String mcUUID) throws IOException {
		CachedAuthUser authUser = authCache.get(mcUUID);

		if (authUser != null && authUser.isValid()) {
			// Already know this user, try and refresh if needed
			refreshUser(authUser);
		} else {
			JSONObject responseData = null;
			try {
				HttpResponse<String> httpResponse = Unirest.post(authUrl).
						header("accept", "application/json").
						field("privatekey", privateKey).
						field("uuid", mcUUID).
						field("name", mcName).
						asString();

				if (!isValidResponseStatus(httpResponse.getStatus())) {
					LOGGER.log(Level.WARNING, "Authentication rejected: " + httpResponse.getStatusText());
					AuthResponse failureResponse = new AuthResponse(null, AuthResponseType.BAD_REQUEST);
					if (httpResponse.getStatus() == 404) {
						failureResponse.authResponseType = AuthResponseType.USER_NOT_FOUND;
					} else if (httpResponse.getStatus() == 403) {
						failureResponse.authResponseType = AuthResponseType.USER_NOT_SUB;
					}
					return failureResponse;
				}
				responseData = new JsonNode(httpResponse.getBody()).getObject();

			} catch (UnirestException e) {
				LOGGER.log(Level.WARNING, "Authentication failed: " + mcName);
				e.printStackTrace();
				return new AuthResponse(authUser, AuthResponseType.BAD_RESPONSE);
			}

			authUser = new DestinyGGUserImpl(responseData.getString("nick"), responseData.getLong("end"));
			authUser.setMCName(mcName);
			authUser.setMCUUID(mcUUID);
		}

		if (authUser.isValid()) {
			// Valid destiny.gg user linked, check subscription and cache
			authCache.put(mcUUID, authUser);
			if (authUser.isSubscriptionExpired()) {
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
