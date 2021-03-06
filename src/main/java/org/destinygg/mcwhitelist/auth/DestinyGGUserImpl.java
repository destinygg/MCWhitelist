/**
 *
 */
package org.destinygg.mcwhitelist.auth;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link CachedAuthUser} implementation for storing a destiny.gg user's
 * authenticated data, and providing necessary validation
 *
 * @author xtphty
 */
public class DestinyGGUserImpl implements CachedAuthUser {
    private String login;

    // When the user's sub expires
    private Long subscriptionEndTimestamp;
    // When the currently cached user data expires
    private Long cacheEndTimestamp;
    // The user's minecraft name
    private String mcName;
    // The user's minecraft uuid
    private String mcUUID;
    // The user's last used ipAddress
    private String ipAddress;

    // Internal fields
    // When the last authorization was made with dgg
    private Long lastAuthTimestamp;
    // When teh last cache refresh was made with dgg
    private Long lastRefreshTimestamp;


    public DestinyGGUserImpl(String login, String ipAddress, Long subscriptionEnd) {
        this.login = login;
        this.subscriptionEndTimestamp = subscriptionEnd;
        this.lastAuthTimestamp = System.currentTimeMillis();
        this.resetCacheTimestamp();
        this.lastRefreshTimestamp = null;
        this.ipAddress = ipAddress;
    }

    @Override
    public String getLoginId() {
        return login;
    }

    @Override
    public String getMCName() {
        return mcName;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public void setMCName(String mcName) {
        this.mcName = mcName;
    }

    @Override
    public String getMCUUID() {
        return mcUUID;
    }

    @Override
    public void setMCUUID(String mcUUID) {
        this.mcUUID = mcUUID;
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(login) && StringUtils.length(mcName) >= 4 && StringUtils.length(mcName) <= 16
                && StringUtils.length(mcUUID) == 36;
    }

    @Override
    public boolean isAuthExpired() {
        return Math.min(TimeUnit.MILLISECONDS.toHours(this.subscriptionEndTimestamp - System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toHours(this.cacheEndTimestamp - System.currentTimeMillis())) < 1;
    }

    @Override
    public void setSubscriptionEndTimestamp(Long timestamp) {
        this.subscriptionEndTimestamp = timestamp;
    }

    @Override
    public void resetCacheTimestamp() {
        this.lastRefreshTimestamp = System.currentTimeMillis();
        this.cacheEndTimestamp = this.lastRefreshTimestamp +
                Math.min(this.subscriptionEndTimestamp, TimeUnit.DAYS.toMillis(1));
    }

    @Override
    public Long getLastRefreshTimestamp() {
        return this.lastRefreshTimestamp;
    }

    @Override
    public Long getCacheTTL() {
        return Math.min(TimeUnit.MILLISECONDS.toHours(this.subscriptionEndTimestamp - System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toHours(this.cacheEndTimestamp - System.currentTimeMillis()));
    }

    @Override
    public void invalidateAuth() {
        this.cacheEndTimestamp = -1L;
        this.lastRefreshTimestamp = null;
    }
}
