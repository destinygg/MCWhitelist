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
	public void resetCacheTimestamp();

	public Long getLastRefreshTimestamp();

	public Long getCacheTTL();
}
