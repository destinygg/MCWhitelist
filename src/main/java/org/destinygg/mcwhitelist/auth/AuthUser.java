/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

/**
 * Spec for authentication objects. Required to provide login, mc name and uuid;
 * and some basic validations.
 * 
 * @author xtphty
 *
 */
public interface AuthUser {
	public String getLoginId();

	public String getMCName();

	public void setMCName(String mcName);

	public String getMCUUID();

	public void setMCUUID(String mcUUID);

	public Long getSubscriptionEndTimestamp();

	public void setSubscriptionEndTimestamp(Long timestamp);

	public boolean isSubscriptionExpired();

	public boolean isValid();
}
