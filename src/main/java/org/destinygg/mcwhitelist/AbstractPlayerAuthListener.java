/**
 * 
 */
package org.destinygg.mcwhitelist;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.destinygg.mcwhitelist.auth.AuthResponse;
import org.destinygg.mcwhitelist.auth.AuthResponse.AuthResponseType;
import org.destinygg.mcwhitelist.auth.AuthService;

/**
 * Provides a common implementation for {@link AuthService} based player white
 * listing, handling the {@link AsyncPlayerPreLoginEvent} and annnouncing player
 * entrance on {@link PlayerJoinEvent}
 * 
 * @author xtphty
 *
 */
public abstract class AbstractPlayerAuthListener implements PlayerAuthListener {
	private static Logger LOGGER = Logger.getLogger(AbstractPlayerAuthListener.class.getName());

	private AuthService authService = null;
	private Set<String> trustedUsers = null;
	
	private boolean enabled = true;

	public abstract AuthService createAuthService(FileConfiguration authConfig);

	@Override
	public void initializeListener(FileConfiguration authConfig) {
		this.authService = createAuthService(authConfig);
		trustedUsers = new HashSet<String>();

		List<String> list = authConfig.getStringList("authentication.trustedUsers");
		for (String user : list) {
			trustedUsers.add(user);
		}
		
		enabled = authConfig.getBoolean("authentication.enable");
	}

	public String getAnnounceMessage(String mcName, String authName) {
		return "User " + ChatColor.RED + authName + ChatColor.WHITE + " connected as " + ChatColor.BLUE + mcName;
	}

	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		if (!enabled) {
			LOGGER.info("MCWhitelist is disabled, allowing " + event.getName());
			return;
		}
		
		LOGGER.info("Trying to authenticate: " + event.getName());
		
		if (trustedUsers.contains(event.getName())){
			LOGGER.info("Trusted user allowed without sub check: " + event.getName());
			event.allow();
			return;
		}

		try {
			AuthResponse authResponse = authService.authenticateUser(event.getName(), event.getUniqueId().toString(),
					event.getAddress().getHostAddress().toString());
			if (!authResponse.authResponseType.isValidResponse()) {
				// User is not valid, reject with failure message
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
						authResponse.authResponseType.getMessage());

			} else if (authResponse.authResponseType == AuthResponseType.VALID_AUTH) {
				LOGGER.info("Authentication successful: " + event.getName());
				event.allow();
			}
		} catch (Exception e) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "Unable to verify subscription");
			LOGGER.severe("Auth request failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		String mcName = event.getPlayer().getName();
		String authName = authService.getPlayerLoginId(event.getPlayer().getUniqueId().toString());
		String message = getAnnounceMessage(mcName, authName);
		if (message != null) {
			Bukkit.broadcastMessage(message);
		}
	}
}
