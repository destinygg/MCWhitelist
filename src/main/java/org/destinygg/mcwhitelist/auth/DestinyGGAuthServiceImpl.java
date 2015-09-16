/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

import static us.monoid.web.Resty.data;
import static us.monoid.web.Resty.form;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;
import us.monoid.web.mime.MultipartContent;

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
	private String refreshUrl;
	private HashMap<String, CachedAuthUser> authCache;

	public DestinyGGAuthServiceImpl(String privateKey, String baseUrl) {
		this.privateKey = privateKey;
		this.baseUrl = baseUrl;
		this.authUrl = this.baseUrl;
		this.refreshUrl = this.baseUrl + "?privatekey=" + privateKey + "&uuid=";
		this.authCache = new HashMap<String, CachedAuthUser>();
	}

	/**
	 * Try and update a valid cached user object if needed
	 * 
	 * @param cachedUser
	 *            the cache item to test
	 * @throws IOException
	 * @throws JSONException
	 */
	public void refreshUser(CachedAuthUser cachedUser) throws IOException, JSONException {
		if (!cachedUser.isCacheExpired()) {
			return;
		}
		JSONResource response = new Resty().json(this.refreshUrl + cachedUser.getMCUUID());
		if (!(response.status(200) || response.status(201) || response.status(202))) {
			LOGGER.log(Level.INFO, "Refresh rejected: " + cachedUser.getMCName());
			return;
		}

		JSONObject data = response.object();
		cachedUser.setSubscriptionEndTimestamp(data.getLong("end"));
		cachedUser.resetCacheTimestamp();
	}

	@Override
	public AuthUser authenticateUser(String mcName, String mcUUID) throws IOException, JSONException {
		CachedAuthUser authUser = authCache.get(mcUUID);

		if (authUser != null && authUser.isValid()) {
			// Refresh user if necessary
			refreshUser(authUser);
			return authUser;
		}

		MultipartContent multiPartContent = form(data("uuid", mcUUID), data("privatekey", privateKey),
				data("name", mcName));
		JSONResource response = new Resty().json(authUrl, multiPartContent);

		if (!(response.status(200) || response.status(201) || response.status(202))) {
			LOGGER.log(Level.WARNING, "Authentication rejected: " + multiPartContent.toString());
			return null;
		}

		JSONObject data = response.object();
		authUser = new DestinyGGUserImpl(data.getString("nick"), data.getLong("end"));
		authUser.setMCName(mcName);
		authUser.setMCUUID(mcUUID);

		if (authUser.isValid()) {
			// Valid response user, add or update in cache
			authCache.put(mcUUID, authUser);
		} else {
			// Invalid response / user, remove from cache and set null
			authCache.put(mcUUID, null);
			authUser = null;
		}

		return authUser;
	}

}
