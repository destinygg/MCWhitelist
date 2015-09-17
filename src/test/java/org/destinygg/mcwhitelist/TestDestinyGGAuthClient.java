/**
 * 
 */
package org.destinygg.mcwhitelist;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
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
 *
 */
public class TestDestinyGGAuthClient {

	private final String VALID_MCUSER = "xtphty";
	private final String VALID_MCUUID = "43685a5f-b5e6-4dfe-a278-fb768575116e";

	private final String INVALID_MCUSER = "hephaestus";
	private final String INVALID_MCUUID = "ea795e60-ac76-43f2-822d-e5878aa35235";

	private static FileConfiguration config;

	@BeforeClass
	public static void beforeClass() throws URISyntaxException {
		config = YamlConfiguration.loadConfiguration(new File(ClassLoader.getSystemResource("config.yml").toURI()));
	}

	@Test
	public void testAuthenticateUser() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
				config.getString("authentication.apiUrl"));
		AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID);
		assertEquals(AuthResponseType.VALID_AUTH, authResponse.authResponseType);
		// sztanpet has xtphty as his mc username on stage.destiny.gg
		assertEquals("sztanpet", authResponse.authUser.getLoginId());

		// Force expired cache
		Long before = System.currentTimeMillis();
		Whitebox.setInternalState(authResponse.authUser, "cacheEndTimestamp",
				System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2));
		assertEquals(true, ((CachedAuthUser) authResponse.authUser).isCacheExpired());
		authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID);
		Long after = System.currentTimeMillis();

		// Makes sure refresh occurred within before/after timestamps
		assertThat(((CachedAuthUser) authResponse.authUser).getLastRefreshTimestamp(), greaterThan(before));
		assertThat(((CachedAuthUser) authResponse.authUser).getLastRefreshTimestamp(), lessThanOrEqualTo(after));
	}

	@Test
	public void testExpiredOrNoSubscription() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
				config.getString("authentication.apiUrl"));
		AuthResponse authResponse = authService.authenticateUser(INVALID_MCUSER, INVALID_MCUUID);
		assertEquals(authResponse.authResponseType, AuthResponseType.USER_NOT_SUB);
	}

	@Test
	public void testMismatchedUUID() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
				config.getString("authentication.apiUrl"));
		AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID.replaceAll("e", "f"));
		assertEquals(authResponse.authResponseType, AuthResponseType.USER_NOT_FOUND);
		// TODO avoid potential exploit with swapping userids
	}

	@Test
	public void testUnknownUser() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(config.getString("authentication.privateKey"),
				config.getString("authentication.apiUrl"));
		AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER + "123456", VALID_MCUUID);
		assertEquals(authResponse.authResponseType, AuthResponseType.USER_NOT_FOUND);
	}

	@Test
	public void testBadPrivateKey() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl("authentication.privateKey", config.getString("authentication.apiUrl"));
		AuthResponse authResponse = authService.authenticateUser(VALID_MCUSER, VALID_MCUUID);
		assertEquals(authResponse.authResponseType, AuthResponseType.BAD_REQUEST);

	}

}
