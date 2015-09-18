/**
 * 
 */
package org.destinygg.mcwhitelist.auth;

/**
 * Cache supporting version of {@link AuthUser}
 * 
 * @author xtphty
 *
 */
public interface CachedAuthUser extends AuthUser {
	public boolean isCacheExpired();

	public void resetCacheTimestamp();

	public Long getLastAuthTimestamp();

	public Long getLastRefreshTimestamp();
}
