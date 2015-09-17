package org.destinygg.mcwhitelist;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.destinygg.mcwhitelist.auth.AuthResponse;
import org.destinygg.mcwhitelist.auth.AuthService;
import org.destinygg.mcwhitelist.auth.AuthUser;
import org.destinygg.mcwhitelist.auth.DestinyGGAuthServiceImpl;
import org.destinygg.mcwhitelist.auth.AuthResponse.AuthResponseType;

/**
 * Listens for Player join and authorizes them with destiny.gg API
 * 
 * @author xtphty
 *
 */
public class DestinyGGPlayerAuthListener implements Listener {
	private static Logger LOGGER = Logger.getLogger(DestinyGGPlayerAuthListener.class.getName());

	private AuthService authService = new DestinyGGAuthServiceImpl(MCWhitelistPlugin.config.getString("authentication.privateKey"),
			MCWhitelistPlugin.config.getString("authentication.apiUrl"));

	@EventHandler
	public void on(AsyncPlayerPreLoginEvent event) {
		if (!MCWhitelistPlugin.config.getBoolean("enabled"))
			return;

		try {
			LOGGER.info("Trying to authenticate: " + event.getName());
			AuthResponse authResponse = authService.authenticateUser(event.getName(), event.getUniqueId().toString());
			if (!authResponse.authResponseType.isValidResponse()) {
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
						authResponse.authResponseType.getMessage());
			} else if (authResponse.authResponseType == AuthResponseType.USER_NOT_FOUND) {
				Bukkit.broadcastMessage("destiny.gg subscriber " + ChatColor.RED + authResponse.authUser.getLoginId() + ChatColor.WHITE
						+ " connected as " + ChatColor.BLUE + event.getName());
				event.allow();
			}
		} catch (Exception e) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "Unable to verify subscription");
			LOGGER.severe("Auth request failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
