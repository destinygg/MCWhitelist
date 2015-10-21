/**
 *
 */
package org.destinygg.mcwhitelist;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.destinygg.mcwhitelist.auth.AuthResponse;
import org.destinygg.mcwhitelist.auth.AuthResponse.AuthResponseType;
import org.destinygg.mcwhitelist.auth.AuthService;
import org.destinygg.mcwhitelist.auth.CachedAuthUser;
import org.destinygg.mcwhitelist.auth.DestinyGGAuthServiceImpl;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

/**
 * Tests proper communication with the DestinyGG API
 *
 * @author xtphty
 */
public class TestDestinyGGAuthClient {

    private final String VALID_MCUSER = "xtphty";
    private final String VALID_MCUUID = "43685a5f-b5e6-4dfe-a278-fb768575116e";

    private final String INVALID_MCUSER = "3er76q8af1";
    private final String INVALID_MCUUID = "ea795e60-ac76-43f2-822d-e5878aa35235";

    private static FileConfiguration config;

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        config = YamlConfiguration.loadConfiguration(new File(ClassLoader.getSystemResource("config.yml").toURI()));
    }

    @Test
    public void testAuthenticateUser() throws IOException, JSONException, URISyntaxException, InterruptedException {
        // Test initial auth
        AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
                config.getString("authentication.apiUrl"));
        AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID, "123");
        assertEquals(AuthResponseType.VALID_AUTH, authResponse.authResponseType);
        assertEquals("xxtphty", authResponse.authUser.getLoginId());
        /////////
        Long before = System.currentTimeMillis();
        Thread.sleep(1000);

        // Test cache usage
        authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID, "123");
        // Makes sure auth request was served from cache, and no refresh was needed
        assertThat((Long) Whitebox.getInternalState(authResponse.authUser, "lastAuthTimestamp"), lessThanOrEqualTo(before));
        assertEquals(null, ((CachedAuthUser) authResponse.authUser).getLastRefreshTimestamp());
        /////////

        // Test cache usage 6 hours later
        Whitebox.setInternalState(authResponse.authUser, "cacheEndTimestamp",
                ((Long) Whitebox.getInternalState(authResponse.authUser, "cacheEndTimestamp")) - TimeUnit.HOURS.toMillis(6));
        authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID, "123");

        // Makes sure auth request was served from cache, and no refresh was needed
        assertThat((Long) Whitebox.getInternalState(authResponse.authUser, "lastAuthTimestamp"), lessThan(before));
        assertEquals(null, ((CachedAuthUser) authResponse.authUser).getLastRefreshTimestamp());
        /////////

        // Test refreshing of expired cache item
        before = System.currentTimeMillis();
        Whitebox.setInternalState(authResponse.authUser, "cacheEndTimestamp",
                System.currentTimeMillis() - 100);
        assertEquals(true, ((CachedAuthUser) authResponse.authUser).isAuthExpired());
        authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID, "123");
        Long after = System.currentTimeMillis();

        // Makes sure cached user with expired cache is refreshed, not re-authenticated
        assertThat(((CachedAuthUser) authResponse.authUser).getLastRefreshTimestamp(), greaterThan(before));
        assertThat(((CachedAuthUser) authResponse.authUser).getLastRefreshTimestamp(), lessThanOrEqualTo(after));
        /////////

        // Test cache is refreshed for new IPs
        authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID, "123456");
        assertEquals(null, ((CachedAuthUser) authResponse.authUser).getLastRefreshTimestamp());
        /////////
    }

    @Test
    public void testExpiredOrNoSubscription() throws IOException, JSONException, URISyntaxException {
        AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
                config.getString("authentication.apiUrl"));
        AuthResponse authResponse = authService.authenticateUser(INVALID_MCUSER, INVALID_MCUUID, "123");
        assertEquals(AuthResponseType.USER_NOT_FOUND, authResponse.authResponseType);
    }

    @Test
    public void testMismatchedUUID() throws IOException, JSONException, URISyntaxException {
        AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
                config.getString("authentication.apiUrl"));
        AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID.replaceAll("e", "f"), "123");
        assertEquals(AuthResponseType.UUID_ALREADY_TAKEN, authResponse.authResponseType);
    }

    @Test
    public void testUnknownUser() throws IOException, JSONException, URISyntaxException {
        AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
                config.getString("authentication.apiUrl"));
        AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER + "123456", VALID_MCUUID, "123");
        assertEquals(AuthResponseType.USER_NOT_FOUND, authResponse.authResponseType);
    }

    @Test
    public void testBadPrivateKey() throws IOException, JSONException, URISyntaxException {
        AuthService authService = new DestinyGGAuthServiceImpl("authentication.privateKey", config.getString("authentication.apiUrl"));
        AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID, "123");
        assertEquals(AuthResponseType.BAD_REQUEST, authResponse.authResponseType);
    }

    @Test
    public void testBannedUser() throws IOException, JSONException, URISyntaxException {
        AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
                config.getString("authentication.apiUrl"));
        AuthResponse authResponse = authService.authenticateUser("200motels", "07e3430c-b47c-413f-bed5-ae65ad7135a3", "123");
        assertEquals(AuthResponseType.USER_BANNED, authResponse.authResponseType);
        authResponse = authService.authenticateUser("BlackerTheBerry", "3b5efd48-d036-445d-a842-38e14d8ba656", "192.168.1.1");
        assertEquals(AuthResponseType.USER_BANNED, authResponse.authResponseType);
    }
}
