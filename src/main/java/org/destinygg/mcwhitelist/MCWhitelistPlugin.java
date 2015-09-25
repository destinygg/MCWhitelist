package org.destinygg.mcwhitelist;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class
 * @author xtphty
 *
 */
public class MCWhitelistPlugin extends JavaPlugin {
	public static JavaPlugin instance;

	public static Logger LOGGER;

	public static FileConfiguration config;
	public static PluginDescriptionFile pdf;

	public static String name;
	public static String version;
	public static String author;

	public void onEnable() {
		LOGGER = getLogger();

		LOGGER.info("Plugin initializing...");

		pdf = getDescription();

		name = pdf.getName();
		version = pdf.getVersion();
		author = pdf.getAuthors().get(0);

		config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
		LOGGER.info("Auth config loaded::\n\turl: " + config.getString("authentication.apiUrl") + "\n\tprivateKey: "
				+ config.getString("authentication.privateKey").substring(0, 4) + "************"
				+ "\n\tlistenerClassClass: " + config.getString("authentication.listenerClass"));

		LOGGER.info("Setting up listeners...");
		try {
			Class<? extends PlayerAuthListener> clazz = Class.forName(config.getString("authentication.listenerClass"))
					.asSubclass(PlayerAuthListener.class);
			PlayerAuthListener instance = clazz.newInstance();
			instance.initializeListener(config);
			getServer().getPluginManager().registerEvents(instance, this);

		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
			LOGGER.severe("Auth listener load failed, shutting down...");
			Bukkit.getServer().shutdown();
		}

		instance = this;
	}

	public void onDisable() {

	}

}
