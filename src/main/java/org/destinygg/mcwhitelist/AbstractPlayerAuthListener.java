/**
 * 
 */
package org.destinygg.mcwhitelist;

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

	public abstract AuthService createAuthService(FileConfiguration authConfig);

	public String getAnnounceMessage(String mcName, String authName) {
		return "User " + ChatColor.RED + authName + ChatColor.WHITE + " connected as " + ChatColor.BLUE + mcName;
	}

	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		LOGGER.info("Trying to authenticate: " + event.getName());

		if (this.authService == null) {
			this.authService = createAuthService(MCWhitelistPlugin.config);
		}

		try {
			AuthResponse authResponse = authService.authenticateUser(event.getName(), event.getUniqueId().toString());
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
