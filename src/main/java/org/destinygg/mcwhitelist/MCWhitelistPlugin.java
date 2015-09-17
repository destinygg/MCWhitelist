package org.destinygg.mcwhitelist;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

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

		try {
			config = YamlConfiguration.loadConfiguration(new File(ClassLoader.getSystemResource("config.yml").toURI()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		LOGGER.info("Setting up listeners...");
		getServer().getPluginManager().registerEvents(new DestinyGGPlayerAuthListener(), this);
		instance = this;
	}

	public void onDisable() {

	}

}
