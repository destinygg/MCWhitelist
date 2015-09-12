package net.bitjump.bukkit.subwhitelister.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import net.bitjump.bukkit.subwhitelister.SubWhitelister;
import net.bitjump.bukkit.subwhitelister.util.WhitelistManager;

public class PlayerListener implements Listener {

	public class DelayedWhitelistTest implements Runnable {
		private Player player;

		public DelayedWhitelistTest(Player p) {
			// TODO Auto-generated constructor stub
			this.player = p;
		}

		@Override
		public void run() {
			// TODO: Authenticate with destiny.gg/check
			if (!player.hasPermission("subwhitelister.exempt")) {
				if (!WhitelistManager.getUsers().contains(player.getName().toLowerCase())) {
					player.kickPlayer("Authentication with destiny.gg failed");
				}
			}

		}

	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (!SubWhitelister.config.getBoolean("enabled"))
			return;
		Player p = e.getPlayer();
		Bukkit.getScheduler().runTaskLater(SubWhitelister.instance, new DelayedWhitelistTest(p),
				SubWhitelister.config.getInt("whitelist.delay") * 20l);
	}
}
