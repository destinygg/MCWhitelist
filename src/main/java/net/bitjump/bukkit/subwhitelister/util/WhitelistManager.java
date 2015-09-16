package net.bitjump.bukkit.subwhitelister.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;

import org.bukkit.Bukkit;

import net.bitjump.bukkit.subwhitelister.SubWhitelister;

public class WhitelistManager {
	private static final HashSet<String> users = new HashSet<String>();

	public static void initialize() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(SubWhitelister.instance, new Runnable() {
			public void run() {
				updateRemoteWhitelists();
			}
		}, 0l, SubWhitelister.config.getInt("whitelist.delay") * 20l);
	}

	public static HashSet<String> getUsers() {
		return users;
	}

	public static void updateRemoteWhitelists() {

		for (String s : SubWhitelister.config.getStringList("whitelist.urls")) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new URL("http://whitelist.twitchapps.com/list.php?id=" + s).openStream()));
				String l;

				while ((l = in.readLine()) != null) {
					users.add(l.trim().toLowerCase());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
