/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

import java.io.IOException;

import org.json.JSONException;

/**
 * @author xtphty
 *
 */
public interface AuthService {

	/**
	 * Authenticates the user with a web service given their MC id information
	 * 
	 * @param mcName
	 *            the user's minecraft name
	 * @param mcUUID
	 *            the user's minecraft UUID
	 * @return An {@link AuthUser} instance if a valid user is linked to this MC
	 *         id , null otherwise. Users access to minecraft may be expired, 
	 *         see {@link AuthUser#isSubscriptionExpired()}
	 * 
	 * @throws IOException
	 *             if an error occurrs when contatic the auth back end
	 * @throws JSONException
	 *             if the server response is invalid
	 */
	public AuthResponse authenticateUser(String mcName, String mcUUID) throws IOException, JSONException;
}
