/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

/**
 * Spec for authentication objects. Required to provide login, mc name and uuid;
 * and some basic validations / introspection details
 * 
 * @author xtphty
 *
 */
public interface AuthUser {
	public String getLoginId();

	public String getMCName();

	public String getIpAddress();

	public void setMCName(String mcName);

	public String getMCUUID();

	public void setMCUUID(String mcUUID);

	public void setSubscriptionEndTimestamp(Long timestamp);

	public boolean isAuthExpired();

	public void invalidateAuth();

	/**
	 * @return isValid true if the user's minecraft data is valid
	 */
	public boolean isValid();
}
