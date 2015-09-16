package org.destinygg.mcwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.destinygg.mcwhitelist.auth.AuthService;
import org.destinygg.mcwhitelist.auth.AuthUser;
import org.destinygg.mcwhitelist.auth.DestinyGGAuthServiceImpl;

import net.bitjump.bukkit.subwhitelister.SubWhitelister;

/**
 * Listens for Player join and authorizes them with destiny.gg API
 * 
 * @author xtphty
 *
 */
public class DestinyGGAuthPlayerListenerImpl implements Listener {

	private AuthService authService = new DestinyGGAuthServiceImpl(SubWhitelister.config.getString("dgg.privateKey"),
			SubWhitelister.config.getString("dgg.baseUrl"));

	/**
	 * Runs the auth API call asynchronously.
	 */
	public class DelayedWhitelistCheck implements Runnable {
		private Player player;

		public DelayedWhitelistCheck(Player p) {
			this.player = p;
		}

		@Override
		public void run() {
			try {
				SubWhitelister.LOGGER.info("Trying to authenticate: " + player.getName());
				AuthUser user = authService.authenticateUser(player.getName(), player.getUniqueId().toString());
				if (!user.isValid() || user.isSubscriptionExpired()) {
					player.kickPlayer("Your subscription has expired");
				} else {
					Bukkit.broadcastMessage("destiny.gg subscriber " + ChatColor.RED + user.getLoginId()
							+ ChatColor.WHITE + " connected as " + ChatColor.BLUE + player.getName());
				}
			} catch (Exception e) {
				// TODO can't kick player in async task, need to create a new sync task here to do the kick
				//player.kickPlayer(e.getMessage());
				SubWhitelister.LOGGER.severe("Auth request failed: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (!SubWhitelister.config.getBoolean("enabled"))
			return;
		Player p = e.getPlayer();

		// TODO disable all of player's abilities until whitelist is confirmed
		Bukkit.getScheduler().runTaskAsynchronously(SubWhitelister.instance, new DelayedWhitelistCheck(p));
	}
}
