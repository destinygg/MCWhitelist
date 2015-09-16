/**
 * 
 */
package org.destinygg.mcwhitelist;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.destinygg.mcwhitelist.auth.AuthService;
import org.destinygg.mcwhitelist.auth.CachedAuthUser;
import org.destinygg.mcwhitelist.auth.DestinyGGAuthServiceImpl;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import us.monoid.json.JSONException;

/**
 * Tests proper communication with the DestinyGG API
 * 
 * @author xtphty
 *
 */
public class TestDestinyGGAuthClient extends AbstractMCPluginTest {

	private final String VALID_MCUSER = "xtphty";
	private final String VALID_MCUUID = "43685a5f-b5e6-4dfe-a278-fb768575116e";

	private final String INVALID_MCUSER = "hephaestus";
	private final String INVALID_MCUUID = "ea795e60-ac76-43f2-822d-e5878aa35235";

	@Test
	public void testAuthenticateUser() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(getMockPlugin().config.getString("dgg.privateKey"),
				getMockPlugin().config.getString("dgg.baseUrl"));
		CachedAuthUser user = (CachedAuthUser) authService.authenticateUser(VALID_MCUSER, VALID_MCUUID);
		assertEquals(true, user.isValid());
		assertEquals(false, user.isSubscriptionExpired());
		// sztanpet has xtphty as his mc username on stage.destiny.gg
		assertEquals("sztanpet", user.getLoginId());

		// Force expired cache
		Long before = System.currentTimeMillis();
		Whitebox.setInternalState(user, "cacheEndTimestamp", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2));
		assertEquals(true, user.isCacheExpired());
		user = (CachedAuthUser) authService.authenticateUser(VALID_MCUSER, VALID_MCUUID);
		Long after = System.currentTimeMillis();

		// Makes sure refresh occurred within before/after timestamps
		assertThat(user.getLastRefreshTimestamp(), greaterThan(before));
		assertThat(user.getLastRefreshTimestamp(), lessThanOrEqualTo(after));
	}

	@Test
	public void testExpiredOrNoSubscription() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(getMockPlugin().config.getString("dgg.privateKey"),
				getMockPlugin().config.getString("dgg.baseUrl"));
		CachedAuthUser user = (CachedAuthUser) authService.authenticateUser(INVALID_MCUSER, INVALID_MCUUID);
		assertTrue(user.isSubscriptionExpired());
		// TODO If MCName is found but user is unsubbed end=0 should be
		// returned. This will allow just refreshing for updates, and
		// giving a more helpful kick message
	}

	@Test
	public void testMismatchedUUID() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(getMockPlugin().config.getString("dgg.privateKey"),
				getMockPlugin().config.getString("dgg.baseUrl"));
		try {
			// TODO Looks like UUID not being validated by API, could allow
			// swapping MCIds
			authService.authenticateUser(VALID_MCUSER, VALID_MCUUID.replaceAll("e", "f"));
			fail("Bad UUID accepted");
		} catch (Exception e) {
			assertThat(e.getMessage(), containsString("403"));
			assertThat(e.getMessage(), containsString("privatekey"));
			e.printStackTrace();
		}
	}

	@Test
	public void testUnknownUser() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl(getMockPlugin().config.getString("dgg.privateKey"),
				getMockPlugin().config.getString("dgg.baseUrl"));
		try {
			authService.authenticateUser(VALID_MCUSER + "123456", VALID_MCUUID);
			fail("Bad UUID accepted");
		} catch (Exception e) {
			assertThat(e.getMessage(), containsString("403"));
			assertThat(e.getMessage(), containsString("nameNotFound"));
			e.printStackTrace();
		}
	}

	@Test
	public void testBadPrivateKey() throws IOException, JSONException, URISyntaxException {
		AuthService authService = new DestinyGGAuthServiceImpl("SWEATSTINY",
				getMockPlugin().config.getString("dgg.baseUrl"));
		try {
			authService.authenticateUser(VALID_MCUSER, VALID_MCUUID);
			fail("Bad key accepted");
		} catch (Exception e) {
			assertThat(e.getMessage(), containsString("403"));
			assertThat(e.getMessage(), containsString("privatekey"));
			e.printStackTrace();
		}
	}

}
