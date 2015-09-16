/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

/**
 * @author xtphty
 *
 */
public interface CachedAuthUser extends AuthUser {
	public boolean isCacheExpired();

	public void resetCacheTimestamp();

	public Long getLastAuthTimestamp();

	public Long getLastRefreshTimestamp();
}
